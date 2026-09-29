package com.example.robotwebsite;

import com.example.robotwebsite.util.KoreanTranslationUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class KoreanTranslationUtilTest {

    @Test
    public void testContainsKorean() {
        assertTrue(KoreanTranslationUtil.containsKorean("신진서"));
        assertTrue(KoreanTranslationUtil.containsKorean("제25회 농심신라면배"));
        assertTrue(KoreanTranslationUtil.containsKorean("흑 불계승"));
        assertFalse(KoreanTranslationUtil.containsKorean("一力遼"));
        assertFalse(KoreanTranslationUtil.containsKorean("B+R"));
        assertFalse(KoreanTranslationUtil.containsKorean("2026-09-27"));
    }

    @Test
    public void testTranslateResult() {
        assertEquals("黒中押し勝ち", KoreanTranslationUtil.translateResult("흑 불계승"));
        assertEquals("黒中押し勝ち", KoreanTranslationUtil.translateResult("흑불계승"));
        assertEquals("白中押し勝ち", KoreanTranslationUtil.translateResult("백 불계승"));
        assertEquals("黒時間切れ勝ち", KoreanTranslationUtil.translateResult("흑 시간승"));
        assertEquals("白時間切れ勝ち", KoreanTranslationUtil.translateResult("백 시간승"));
        assertEquals("黒反則勝ち", KoreanTranslationUtil.translateResult("흑 반칙승"));
        assertEquals("白反則勝ち", KoreanTranslationUtil.translateResult("백 반칙승"));
        assertEquals("黒半目勝ち", KoreanTranslationUtil.translateResult("흑 반집승"));
        assertEquals("白半目勝ち", KoreanTranslationUtil.translateResult("백 반집승"));
        assertEquals("黒3.5目勝ち", KoreanTranslationUtil.translateResult("흑 3.5집승"));
        assertEquals("白1目半勝ち", KoreanTranslationUtil.translateResult("백 1집반승"));
        assertEquals("白2.5目勝ち", KoreanTranslationUtil.translateResult("백 2.5집승"));
        assertEquals("持碁", KoreanTranslationUtil.translateResult("무승부"));
        assertEquals("持碁", KoreanTranslationUtil.translateResult("빅"));

        // English format
        assertEquals("黒中押し勝ち", KoreanTranslationUtil.translateResult("B+R"));
        assertEquals("白中押し勝ち", KoreanTranslationUtil.translateResult("W+R"));
        assertEquals("黒中押し勝ち", KoreanTranslationUtil.translateResult("B+Resign"));
        assertEquals("黒半目勝ち", KoreanTranslationUtil.translateResult("B+0.5"));
        assertEquals("黒3.5目勝ち", KoreanTranslationUtil.translateResult("B+3.5"));
        assertEquals("白0.5目勝ち", KoreanTranslationUtil.translateResult("W+0.5"));
    }

    @Test
    public void testTranslatePlayerName() {
        assertEquals("申真諝 九段", KoreanTranslationUtil.translatePlayerName("신진서 9단"));
        assertEquals("朴廷桓 九段", KoreanTranslationUtil.translatePlayerName("박정환 9단"));
        assertEquals("卞相壹 九段", KoreanTranslationUtil.translatePlayerName("변상일 9단"));
        assertEquals("崔精 九段", KoreanTranslationUtil.translatePlayerName("최정 9단"));
        assertEquals("金恩持 九段", KoreanTranslationUtil.translatePlayerName("김은지 9단"));
        assertEquals("柯潔 九段", KoreanTranslationUtil.translatePlayerName("커제 9단"));
        assertEquals("辜梓豪 九段", KoreanTranslationUtil.translatePlayerName("구쯔하오 9단"));
        assertEquals("一力遼 九段", KoreanTranslationUtil.translatePlayerName("이치리키 료 9단"));
        assertEquals("仲邑菫 三段", KoreanTranslationUtil.translatePlayerName("나카무라 스미레 3단"));
        assertEquals("芝野虎丸 九段", KoreanTranslationUtil.translatePlayerName("시바노 도라마루 9단"));
        assertEquals("許皓鋐 九段", KoreanTranslationUtil.translatePlayerName("쉬하오홍 9단"));
    }

    @Test
    public void testTranslateMatchName() {
        assertEquals("第25回 農心辛ラーメン杯 世界囲碁最強戦 本戦 第14局",
                KoreanTranslationUtil.translateMatchName("제25회 농심신라면배 세계바둑최강전 본선 14국"));
        assertEquals("第28回 サムスン火災杯 ワールド囲碁マスターズ 決勝 3番勝負 第1局",
                KoreanTranslationUtil.translateMatchName("제28회 삼성화재배 월드바둑마스터스 결승 3번기 1국"));
        assertEquals("第29回 LG杯 朝鮮日報棋王戦 準決勝",
                KoreanTranslationUtil.translateMatchName("제29회 LG배 조선일보 기왕전 준결승"));
        assertEquals("第1回 南洋杯 32強",
                KoreanTranslationUtil.translateMatchName("제1회 난양배 32강"));
        assertEquals("第1回 南洋杯 世界囲碁マスターズ 32強",
                KoreanTranslationUtil.translateMatchName("제1회 난양배 세계바둑마스터스 32강"));
        assertEquals("第2回 YK建機杯 プロ棋戦 本戦 第1ラウンド 第1局",
                KoreanTranslationUtil.translateMatchName("제2회 YK건기배 프로기전 본선 1라운드 1국"));
        assertEquals("第10回 グロービス杯 世界囲碁U-20 決勝",
                KoreanTranslationUtil.translateMatchName("제10회 글로비스배 세계바둑U-20 결승"));
        assertEquals("第8回 天台山杯 本戦 第1回戦",
                KoreanTranslationUtil.translateMatchName("제8회 천태산배 본선 1회전"));
        assertEquals("新韓銀行杯 本戦 第1局",
                KoreanTranslationUtil.translateMatchName("신한은행배 본선 1국"));
    }
}
