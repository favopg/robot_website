package com.example.robotwebsite;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class PlayerLikeTest {

    @LocalServerPort
    private int port;

    @Test
    void testPlayerLikeLimit() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();
            page.navigate("http://localhost:" + port + "/match-results");

            // 推し棋士登録モーダルが出たら閉じる
            if (page.isVisible("#favoriteModalCloseBtn")) {
                page.click("#favoriteModalCloseBtn");
            }

            // 5人の異なる棋士に対していいねを押す
            for (int i = 0; i < 5; i++) {
                Locator playerLinks = page.locator(".player-detail-trigger");
                playerLinks.nth(i).waitFor(new Locator.WaitForOptions().setTimeout(10000));
                playerLinks.nth(i).click();

                page.waitForSelector("#playerModal.show");

                Locator likeBtn = page.locator("#likeButton");
                Locator limitMsg = page.locator("#likeLimitMessage");

                // まだいいねしていない状態
                assertFalse(likeBtn.isDisabled(), "Button should be enabled for player " + i);
                
                likeBtn.click();
                
                // いいねした後は無効化されるはず（一人一回まで）
                page.waitForCondition(likeBtn::isDisabled);
                assertEquals("この棋士には既にいいねしました", limitMsg.textContent().trim());

                // モーダルを閉じる
                page.keyboard().press("Escape");
                page.waitForSelector("#playerModal.show", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
            }

            // 6人目の棋士を開く
            Locator sixthPlayer = page.locator(".player-detail-trigger").nth(5);
            sixthPlayer.click();
            page.waitForSelector("#playerModal.show");

            // 合計5回に達しているので、最初から無効化されているはず
            Locator likeBtn = page.locator("#likeButton");
            Locator limitMsg = page.locator("#likeLimitMessage");
            assertTrue(likeBtn.isDisabled(), "Button should be disabled after total 5 likes");
            assertEquals("合計いいね数が上限（5回）に達しました", limitMsg.textContent().trim());

            browser.close();
        }
    }
}
