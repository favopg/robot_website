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

    public NihonkiinPlayerScraperTasklet(NihonkiinPlayerScraper nihonkiinPlayerScraper) {
        this.nihonkiinPlayerScraper = nihonkiinPlayerScraper;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // タスクの説明では「既存の NihonkiinMatchScraperTasklet が対局情報を取得する際...拡張します」
        // とあるので、ここはプレースホルダー、または特定の棋士を強制的に取得する場合に使用します。
        
        // 例として、課題に記載された棋士を取得してみる
        nihonkiinPlayerScraper.scrapePlayerDetail("許家元", "https://www.nihonkiin.or.jp/player/htm/ki000385.html");
        
        return RepeatStatus.FINISHED;
    }
}
