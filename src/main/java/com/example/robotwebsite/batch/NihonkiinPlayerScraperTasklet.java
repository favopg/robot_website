package com.example.robotwebsite.batch;

import com.example.robotwebsite.repository.PlayerRepository;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
public class NihonkiinPlayerScraperTasklet implements Tasklet {

    private final NihonkiinPlayerScraper nihonkiinPlayerScraper;
    private final KansaikiinPlayerScraper kansaikiinPlayerScraper;

    public NihonkiinPlayerScraperTasklet(NihonkiinPlayerScraper nihonkiinPlayerScraper, KansaikiinPlayerScraper kansaikiinPlayerScraper) {
        this.nihonkiinPlayerScraper = nihonkiinPlayerScraper;
        this.kansaikiinPlayerScraper = kansaikiinPlayerScraper;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // タスクの説明では「既存の NihonkiinMatchScraperTasklet が対局情報を取得する際...拡張します」
        // とあるので、ここはプレースホルダー、または特定の棋士を強制的に取得する場合に使用します。
        
        // 特定の棋士の情報を強制的に取得・更新する
        nihonkiinPlayerScraper.scrapePlayerDetail("許家元", "https://www.nihonkiin.or.jp/player/htm/ki000385.html");
        nihonkiinPlayerScraper.scrapePlayerDetail("張豊猷", "https://www.nihonkiin.or.jp/player/htm/ki000331.html");
        nihonkiinPlayerScraper.scrapePlayerDetail("王銘琬", "https://www.nihonkiin.or.jp/player/htm/ki000185.html");
        nihonkiinPlayerScraper.scrapePlayerDetail("卞聞愷", "https://www.nihonkiin.or.jp/player/htm/ki000449.html");
        
        // 関西棋院の棋士
        kansaikiinPlayerScraper.scrapePlayerDetail("髙山邊楓実", "https://kansaikiin.jp/kisi_prof/takayamabefumi.html");
        
        // 日本棋院の棋士（追加分）
        nihonkiinPlayerScraper.scrapePlayerDetail("フィトラ・R・S", "https://www.nihonkiin.or.jp/player/htm/ki000504.html");
        
        return RepeatStatus.FINISHED;
    }
}
