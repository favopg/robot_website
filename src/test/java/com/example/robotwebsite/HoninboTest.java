package com.example.robotwebsite;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class HoninboTest {

    @LocalServerPort
    private int port;

    @Test
    void testHoninboMenu() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            // トップページにアクセス
            page.navigate("http://localhost:" + port + "/");

            // 「囲碁棋戦」ドロップダウンが表示されているか確認
            assertTrue(page.isVisible("text=囲碁棋戦"));

            // ドロップダウンをクリック
            page.click("text=囲碁棋戦");

            // 「本因坊戦」メニューが表示されているか確認
            assertTrue(page.isVisible("text=本因坊戦"));

            // 「本因坊戦」をクリックして遷移
            page.click("text=本因坊戦");

            // URLが正しいか確認
            assertTrue(page.url().contains("/matches/title/%E6%9C%AC%E5%9B%A0%E5%9D%8A%E6%88%A6"));

            // ページタイトルが正しいか確認 (Thymeleafで ${title} の対局一覧)
            assertTrue(page.innerText("h1").contains("本因坊戦 の対局一覧"));

            browser.close();
        }
    }
}
