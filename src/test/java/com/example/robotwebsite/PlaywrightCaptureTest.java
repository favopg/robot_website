package com.example.robotwebsite;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PlaywrightCaptureTest {

    @LocalServerPort
    private int port;

    @Test
    void captureAndSaveToExcel() throws Exception {
        Path captureDir = Paths.get("captures");
        if (Files.exists(captureDir)) {
            // フォルダの中身を削除
            Files.walkFileTree(captureDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (!dir.equals(captureDir)) {
                        Files.delete(dir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            Files.createDirectories(captureDir);
        }

        String screenshotName = "screenshot_" + System.currentTimeMillis() + ".png";
        Path screenshotPath = captureDir.resolve(screenshotName);
        String excelName = "capture_report_" + System.currentTimeMillis() + ".xlsx";
        Path excelPath = captureDir.resolve(excelName);

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();
            page.navigate("http://localhost:" + port);
            
            // キャプチャを取得
            page.screenshot(new Page.ScreenshotOptions().setPath(screenshotPath));
            
            browser.close();
        }

        // エクセルに保存
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Capture");
            
            // 画像を読み込む
            try (InputStream is = new FileInputStream(screenshotPath.toFile())) {
                byte[] bytes = IOUtils.toByteArray(is);
                int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
                
                CreationHelper helper = workbook.getCreationHelper();
                Drawing<?> drawing = sheet.createDrawingPatriarch();
                ClientAnchor anchor = helper.createClientAnchor();
                
                // 画像の配置位置
                anchor.setCol1(1);
                anchor.setRow1(1);
                
                Picture pict = drawing.createPicture(anchor, pictureIdx);
                pict.resize(); // 元のサイズでリサイズ
            }

            try (FileOutputStream fileOut = new FileOutputStream(excelPath.toFile())) {
                workbook.write(fileOut);
            }
        }
        
        System.out.println("Screenshot saved to: " + screenshotPath.toAbsolutePath());
        System.out.println("Excel saved to: " + excelPath.toAbsolutePath());
    }
}
