package com.example.robotwebsite.util;

import com.example.robotwebsite.dto.KifuInfo;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KoreanTranslationUtil {

    private static final Pattern KOREAN_PATTERN = Pattern.compile("[\\uac00-\\ud7af\\u1100-\\u11ff\\u3130-\\u318f]");

    // 棋士名辞書（韓国語 -> 日本語表記/漢字）
    private static final Map<String, String> PLAYER_MAP = new LinkedHashMap<>();

    // 棋戦・用語辞書（韓国語 -> 日本語表記）
    private static final Map<String, String> TOURNAMENT_TERMS_MAP = new LinkedHashMap<>();

    // 段級位辞書
    private static final Map<String, String> RANK_MAP = new LinkedHashMap<>();

    static {
        // --- 棋士名マッピング ---
        // 韓国トップ棋士 & プロ棋士
        addPlayer("신진서", "申真諝");
        addPlayer("박정환", "朴廷桓");
        addPlayer("변상일", "卞相壹");
        addPlayer("신민준", "申旻埈");
        addPlayer("강동윤", "姜東潤");
        addPlayer("김지석", "金志錫");
        addPlayer("김명훈", "金明訓");
        addPlayer("설현준", "偰玹準");
        addPlayer("원성진", "元晟溱");
        addPlayer("안성준", "安成浚");
        addPlayer("이지현", "李志賢");
        addPlayer("박건호", "朴鍵昊");
        addPlayer("한승주", "韓昇周");
        addPlayer("류민형", "柳珉瀅");
        addPlayer("홍성지", "洪性志");
        addPlayer("이원영", "李元栄");
        addPlayer("박영훈", "朴永訓");
        addPlayer("목진석", "睦鎮碩");
        addPlayer("조한승", "趙漢乗");
        addPlayer("윤준상", "尹畯相");
        addPlayer("최철한", "崔哲瀚");
        addPlayer("송지훈", "宋知勲");
        addPlayer("허영호", "許映皓");
        addPlayer("백홍석", "白洪淅");
        addPlayer("진시영", "陳時映");
        addPlayer("안국현", "安国鉉");
        addPlayer("나현", "羅玄");
        addPlayer("이동훈", "李東勲");
        addPlayer("강유택", "姜儒澤");
        addPlayer("이영구", "李映九");
        addPlayer("조혜연", "趙恵連");
        addPlayer("김채영", "金彩瑛");
        addPlayer("김다영", "金多瑛");
        addPlayer("허서현", "許瑞玹");
        addPlayer("조승아", "曺承亜");
        addPlayer("김혜민", "金恵敏");
        addPlayer("박지은", "朴志恩");
        addPlayer("박소현", "朴昭炫");
        addPlayer("오정아", "呉政娥");
        addPlayer("김미리", "金美里");
        addPlayer("이민진", "李玟真");
        addPlayer("문민종", "文敏鍾");
        addPlayer("한우진", "韓友賑");
        addPlayer("박상진", "朴常鎮");
        addPlayer("유창혁", "劉昌赫");
        addPlayer("서봉수", "徐奉洙");
        addPlayer("양재호", "梁宰豪");
        addPlayer("서능욱", "徐能旭");
        addPlayer("김수장", "金秀壮");
        addPlayer("권갑용", "権甲龍");
        addPlayer("장수영", "張秀英");
        addPlayer("백성호", "白成豪");
        addPlayer("노영하", "盧永夏");
        addPlayer("김인", "金寅");
        addPlayer("이창호", "李昌鎬");
        addPlayer("이세돌", "李世乭");
        addPlayer("최정", "崔精");
        addPlayer("김은지", "金恩持");
        addPlayer("오유진", "呉侑珍");
        addPlayer("조훈현", "曺薫鉉");
        addPlayer("심재익", "沈載益");
        addPlayer("이창석", "李昌錫");
        addPlayer("박하민", "朴河旼");
        addPlayer("한상훈", "韓尚勲");
        addPlayer("안조영", "安祚永");
        addPlayer("김승준", "金承俊");
        addPlayer("이상훈", "李相勲");
        addPlayer("윤현석", "尹炫晳");
        addPlayer("양건", "梁建");
        addPlayer("양우석", "梁禹錫");
        addPlayer("김성룡", "金成龍");
        addPlayer("김영삼", "金栄三");
        addPlayer("최원용", "崔原踊");
        addPlayer("김주호", "金主鎬");
        addPlayer("고근태", "高根台");
        addPlayer("홍민표", "洪旼杓");
        addPlayer("이현호", "李賢虎");
        addPlayer("박승화", "朴承華");
        addPlayer("김진휘", "金真輝");
        addPlayer("위태웅", "魏太雄");
        addPlayer("류수항", "柳秀沆");
        addPlayer("김세동", "金世東");
        addPlayer("한웅규", "韓雄奎");
        addPlayer("박민규", "朴珉奎");
        addPlayer("황재연", "黄宰淵");
        addPlayer("이원도", "李元道");
        addPlayer("최홍윤", "崔烘胤");
        addPlayer("배준희", "裵俊煕");
        addPlayer("옥득진", "玉得真");
        addPlayer("김기용", "金起用");
        addPlayer("최기훈", "崔基勲");
        addPlayer("이형진", "李炯珍");
        addPlayer("문유빈", "文儒彬");
        addPlayer("권효진", "権孝珍");
        addPlayer("정서준", "鄭誓儁");
        addPlayer("현유빈", "玄裕斌");
        addPlayer("이연", "李沇");
        addPlayer("김선기", "金宣岐");
        addPlayer("김범서", "金凡瑞");
        addPlayer("강우혁", "姜又赫");
        addPlayer("김윤태", "金潤泰");
        addPlayer("최광호", "崔光浩");
        addPlayer("백현우", "白現宇");
        addPlayer("곽원근", "郭圓根");
        addPlayer("윤민중", "尹潣重");
        addPlayer("박진솔", "朴進率");
        addPlayer("유오성", "柳旿成");
        addPlayer("정연우", "鄭延宇");
        addPlayer("김유찬", "金裕燦");
        addPlayer("송규상", "宋圭相");
        addPlayer("박재근", "朴材根");
        addPlayer("양민석", "梁民錫");
        addPlayer("김영도", "金栄徒");
        addPlayer("원제훈", "元齊焄");
        addPlayer("김승진", "金昇珍");
        addPlayer("박신영", "朴伸英");
        addPlayer("김강민", "金康旼");
        addPlayer("이의현", "李義賢");
        addPlayer("조완규", "趙完珪");
        addPlayer("양유준", "梁洧準");
        addPlayer("허영락", "許栄珞");
        addPlayer("윤성우", "尹聖佑");
        addPlayer("최은규", "崔恩奎");
        addPlayer("이슬주", "李スルジュ");
        addPlayer("김민서", "金珉舒");
        addPlayer("정유진", "丁有珍");
        addPlayer("고미소", "高ミソ");
        addPlayer("김경은", "金京垠");
        addPlayer("이도현", "李度弦");
        addPlayer("김효영", "金孝英");
        addPlayer("이서영", "李瑞英");
        addPlayer("유주현", "柳珠儇");
        addPlayer("박소율", "朴昭律");
        addPlayer("김선빈", "金先彬");
        addPlayer("권주리", "権周利");
        addPlayer("강다정", "姜多情");
        addPlayer("송혜령", "宋慧領");
        addPlayer("차주혜", "車珠恵");
        addPlayer("장혜령", "張兮領");
        addPlayer("이정은", "李晶恩");
        addPlayer("김상인", "金相仁");
        addPlayer("김제나", "金制拏");
        addPlayer("백지희", "白知熙");
        addPlayer("배윤진", "裵允珍");
        addPlayer("이영신", "李英信");
        addPlayer("하호정", "河好貞");
        addPlayer("김은선", "金恩善");
        addPlayer("현미진", "玄味真");
        addPlayer("윤영민", "尹暎善");
        addPlayer("김선미", "金善美");
        addPlayer("한해원", "韓海苑");
        addPlayer("김혜림", "金恵臨");
        addPlayer("문도원", "文度媛");
        addPlayer("김나현", "金娜賢");
        addPlayer("김신영", "金伸英");
        addPlayer("이유진", "李裕真");
        addPlayer("강지수", "姜智洙");
        addPlayer("정동식", "鄭東植");
        addPlayer("김동호", "金東昊");
        addPlayer("김정현", "金庭賢");
        addPlayer("김성진", "金成進");
        addPlayer("박진열", "朴振烈");
        addPlayer("김일환", "金日煥");
        addPlayer("서건석", "徐健石");
        addPlayer("차민수", "車敏洙");
        addPlayer("권오민", "権五敏");
        addPlayer("이태현", "李泰賢");
        addPlayer("조인선", "趙寅善");
        addPlayer("황진형", "黄鎮亨");
        addPlayer("김동우", "金東佑");
        addPlayer("이재웅", "李在雄");
        addPlayer("박병규", "朴炳奎");
        addPlayer("김형우", "金炯佑");
        addPlayer("이희성", "李煕星");
        addPlayer("안형준", "安亨浚");
        addPlayer("김덕규", "金徳奎");
        addPlayer("한철균", "韓鉄均");
        addPlayer("김동엽", "金東燁");
        addPlayer("박상돈", "朴相敦");
        addPlayer("고재희", "高在煕");
        addPlayer("김철중", "金哲中");
        addPlayer("이홍열", "李洪烈");
        addPlayer("황원준", "黄元俊");
        addPlayer("정대상", "鄭大相");
        addPlayer("김종수", "金宗洙");
        addPlayer("유건재", "劉健在");
        addPlayer("나종훈", "羅鐘勲");
        addPlayer("김광식", "金光植");
        addPlayer("김성래", "金成来");
        addPlayer("백대현", "白大鉉");
        addPlayer("김강근", "金江根");
        addPlayer("전영규", "全瑛圭");
        addPlayer("유재호", "柳才馨");
        addPlayer("온소진", "温昭珍");
        addPlayer("김진훈", "金真勲");
        addPlayer("김현섭", "金顕燮");
        addPlayer("박종욱", "朴鍾昱");
        addPlayer("박대영", "朴大英");
        addPlayer("윤찬희", "尹燦煕");
        addPlayer("박경근", "朴硬根");
        addPlayer("김원빈", "金元彬");
        addPlayer("김민호", "金民浩");
        addPlayer("박정수", "朴正洙");
        addPlayer("박시열", "朴時烈");
        addPlayer("박승현", "朴昇賢");
        addPlayer("박주민", "朴柱民");
        addPlayer("박지현", "朴只玹");
        addPlayer("박현수", "朴炫洙");
        addPlayer("임상헌", "林相憲");
        addPlayer("임진욱", "林珍郁");
        addPlayer("임훈", "林勲");
        addPlayer("정훈현", "鄭薫鉉");
        addPlayer("조기환", "曺基煥");
        addPlayer("조남철", "趙南哲");
        addPlayer("주민호", "朱敏鎬");
        addPlayer("주형욱", "朱亨煜");
        addPlayer("진동규", "陳東奎");
        addPlayer("차수권", "車修権");
        addPlayer("채규우", "蔡圭祐");
        addPlayer("최규병", "崔珪昞");
        addPlayer("최명훈", "崔明勲");
        addPlayer("최문용", "崔文墉");
        addPlayer("최병환", "崔丙煥");
        addPlayer("최우수", "崔優洙");
        addPlayer("최윤상", "崔允商");
        addPlayer("최창원", "崔彰元");
        addPlayer("최현재", "崔現在");
        addPlayer("한종진", "韓鐘振");
        addPlayer("허재원", "許宰源");
        addPlayer("허진", "許進");
        addPlayer("홍기표", "洪基杓");
        addPlayer("홍맑은샘", "洪マルグンセム");
        addPlayer("홍석민", "洪錫敏");
        addPlayer("홍종현", "洪鐘賢");

        // 日本・中国棋士（韓国語ハングル表記）
        addPlayer("이치리키 료", "一力遼");
        addPlayer("이치리키료", "一力遼");
        addPlayer("이야마 유타", "井山裕太");
        addPlayer("이야마유타", "井山裕太");
        addPlayer("시바노 도라마루", "芝野虎丸");
        addPlayer("시바노도라마루", "芝野虎丸");
        addPlayer("쉬자위안", "許家元");
        addPlayer("쿄 카겐", "許家元");
        addPlayer("우에노 아사미", "上野愛咲美");
        addPlayer("우에노아사미", "上野愛咲美");
        addPlayer("후지사와 리나", "藤沢里菜");
        addPlayer("후지사와리나", "藤沢里菜");
        addPlayer("나카무라 스미레", "仲邑菫");
        addPlayer("스미레", "仲邑菫");
        addPlayer("조치훈", "趙治勲");
        addPlayer("린하이펑", "林海峰");
        addPlayer("고바야시 고이치", "小林光一");
        addPlayer("오타케 히데오", "大竹英雄");
        addPlayer("다케미야 마사키", "武宮正樹");
        addPlayer("가토 마사오", "加藤正夫");

        addPlayer("커제", "柯潔");
        addPlayer("구쯔하오", "辜梓豪");
        addPlayer("딩하오", "丁浩");
        addPlayer("양딩신", "楊鼎新");
        addPlayer("왕싱하오", "王星昊");
        addPlayer("리쉬안하오", "李軒豪");
        addPlayer("자오천위", "趙晨宇");
        addPlayer("리웨이칭", "李維清");
        addPlayer("롄샤오", "連笑");
        addPlayer("당이페이", "党毅飛");
        addPlayer("미위팅", "羋昱廷");
        addPlayer("판팅위", "范廷鈺");
        addPlayer("퉈자시", "柁嘉熹");
        addPlayer("셰얼하오", "謝爾豪");
        addPlayer("탄샤오", "檀嘯");
        addPlayer("스웨", "時越");
        addPlayer("장웨이제", "江維傑");
        addPlayer("셰커", "謝科");
        addPlayer("구리", "古力");
        addPlayer("창하오", "常昊");
        addPlayer("마샤오춘", "馬暁春");
        addPlayer("녜웨이핑", "聶衛平");
        addPlayer("천야오예", "陳耀燁");
        addPlayer("저우루이양", "周睿羊");
        addPlayer("위즈잉", "於之瑩");
        addPlayer("저우홍위", "周泓余");
        addPlayer("루민취안", "陸敏全");
        addPlayer("리허", "李赫");
        addPlayer("탕웨이싱", "唐韋星");
        addPlayer("펑리야오", "彭立尭");

        // 段級位
        RANK_MAP.put("9단", "九段");
        RANK_MAP.put("8단", "八段");
        RANK_MAP.put("7단", "七段");
        RANK_MAP.put("6단", "六段");
        RANK_MAP.put("5단", "五段");
        RANK_MAP.put("4단", "四段");
        RANK_MAP.put("3단", "三段");
        RANK_MAP.put("2단", "二段");
        RANK_MAP.put("1단", "初段");
        RANK_MAP.put("초단", "初段");
        RANK_MAP.put("아마", "アマ");
        RANK_MAP.put("프로", "プロ");

        // --- 棋戦名・用語マッピング ---
        addTournamentTerm("농심신라면배", "農心辛ラーメン杯");
        addTournamentTerm("농심배", "農心杯");
        addTournamentTerm("삼성화재배", "サムスン火災杯");
        addTournamentTerm("삼성배", "三星杯");
        addTournamentTerm("월드바둑마스터스", "ワールド囲碁マスターズ");
        addTournamentTerm("월드 바둑 마스터스", "ワールド囲碁マスターズ");
        addTournamentTerm("LG배 조선일보 기왕전", "LG杯 朝鮮日報棋王戦");
        addTournamentTerm("LG배 기왕전", "LG杯 棋王戦");
        addTournamentTerm("LG배", "LG杯");
        addTournamentTerm("국수산맥 국제바둑대회", "国手山脈 国際囲碁大会");
        addTournamentTerm("국수산맥 세계프로최강전", "国手山脈 世界プロ最強戦");
        addTournamentTerm("국수산맥배", "国手山脈杯");
        addTournamentTerm("국수산맥", "国手山脈");
        addTournamentTerm("춘란배 세계바둑선수권", "春蘭杯 世界囲碁選手権");
        addTournamentTerm("춘란배", "春蘭杯");
        addTournamentTerm("몽백합배 세계바둑오픈", "夢百合杯 世界囲碁オープン");
        addTournamentTerm("몽백합배", "夢百合杯");
        addTournamentTerm("잉씨배 세계프로바둑선수권", "応氏杯 世界プロ囲碁選手権");
        addTournamentTerm("잉씨배", "応氏杯");
        addTournamentTerm("응씨배", "応氏杯");
        addTournamentTerm("백령배 세계바둑오픈", "百霊杯 世界囲碁オープン");
        addTournamentTerm("백령배", "百霊杯");
        addTournamentTerm("신진서-박정환 슈퍼매치", "申真諝-朴廷桓 スーパーマッチ");
        addTournamentTerm("슈퍼매치", "スーパーマッチ");
        addTournamentTerm("KB국민은행 바둑리그", "KB国民銀行 囲碁リーグ");
        addTournamentTerm("KB국민은행", "KB国民銀行");
        addTournamentTerm("KB바둑리그", "KB囲碁リーグ");
        addTournamentTerm("한국바둑리그", "韓国囲碁リーグ");
        addTournamentTerm("한국여자바둑리그", "韓国女子囲碁リーグ");
        addTournamentTerm("여자바둑리그", "女子囲碁リーグ");
        addTournamentTerm("쏘팔코사놀 최고기사결정전", "ソパルコサノール最高棋士決定戦");
        addTournamentTerm("쏘팔코사놀배", "ソパルコサノール杯");
        addTournamentTerm("쏘팔코사놀", "ソパルコサノール");
        addTournamentTerm("GS칼텍스배 프로기전", "GSカルテックス杯 プロ棋戦");
        addTournamentTerm("GS칼텍스배", "GSカルテックス杯");
        addTournamentTerm("맥심커피배 입신최강전", "マキシムコーヒー杯 入神最強戦");
        addTournamentTerm("맥심커피배", "マキシムコーヒー杯");
        addTournamentTerm("맥심배", "マキシム杯");
        addTournamentTerm("하찬석국수배 영재최강전", "河燦錫国手杯 英才最強戦");
        addTournamentTerm("하찬석국수배", "河燦錫国手杯");
        addTournamentTerm("하찬석배", "河燦錫杯");
        addTournamentTerm("닥터지 여자최고기사결정전", "Dr.G 女子最高棋士決定戦");
        addTournamentTerm("닥터지", "Dr.G");
        addTournamentTerm("IBK기업은행배 여자바둑마스터스", "IBK企業銀行杯 女子囲碁マスターズ");
        addTournamentTerm("IBK기업은행배", "IBK企業銀行杯");
        addTournamentTerm("취저우 란커배", "衢州爛柯杯");
        addTournamentTerm("취저우 난가배", "衢州爛柯杯");
        addTournamentTerm("란커배", "爛柯杯");
        addTournamentTerm("난가배", "爛柯杯");
        addTournamentTerm("우량예배", "五糧液杯");
        addTournamentTerm("오청원배 세계여자바둑선수권", "呉清源杯 世界女子囲碁選手権");
        addTournamentTerm("오청원배", "呉清源杯");
        addTournamentTerm("궁륭산병성배", "穹窿山兵聖杯");
        addTournamentTerm("센코컵", "SENKO CUP");
        addTournamentTerm("호반배 서울신문 세계여자패왕전", "湖盤杯 ソウル新聞 世界女子覇王戦");
        addTournamentTerm("호반배", "湖盤杯");
        addTournamentTerm("서울신문", "ソウル新聞");
        addTournamentTerm("세계여자패왕전", "世界女子覇王戦");
        addTournamentTerm("아시안게임", "アジア大会");
        addTournamentTerm("한중 슈퍼리그", "韓中スーパーリーグ");
        addTournamentTerm("세계바둑최강전", "世界囲碁最強戦");
        addTournamentTerm("세계프로최강전", "世界プロ最強戦");
        addTournamentTerm("세계바둑선수권", "世界囲碁選手権");
        addTournamentTerm("세계바둑오픈", "世界囲碁オープン");
        addTournamentTerm("최고기사결정전", "最高棋士決定戦");
        addTournamentTerm("여자최고기사결정전", "女子最高棋士決定戦");
        addTournamentTerm("입신최강전", "入神最強戦");
        addTournamentTerm("영재최강전", "英才最強戦");
        addTournamentTerm("신예대항전", "新鋭対抗戦");
        addTournamentTerm("프로기전", "プロ棋戦");
        addTournamentTerm("기왕전", "棋王戦");
        addTournamentTerm("용성전", "竜星戦");
        addTournamentTerm("명인전", "名人戦");
        addTournamentTerm("국수전", "国手戦");
        addTournamentTerm("기성전", "棋聖戦");
        addTournamentTerm("천원전", "天元戦");
        addTournamentTerm("왕위전", "王位戦");
        addTournamentTerm("최고위전", "最高位戦");
        addTournamentTerm("대왕전", "大王戦");
        addTournamentTerm("여류국수전", "女流国手戦");
        addTournamentTerm("여류명인전", "女流名人戦");
        addTournamentTerm("여류기성전", "女流棋聖戦");
        addTournamentTerm("KBS바둑왕전", "KBS囲碁王戦");
        addTournamentTerm("바둑왕전", "囲碁王戦");
        addTournamentTerm("조선일보", "朝鮮日報");
        addTournamentTerm("통합예선", "統合予選");
        addTournamentTerm("최종예선", "最終予選");
        addTournamentTerm("최종국", "最終局");
        addTournamentTerm("결승전", "決勝戦");
        addTournamentTerm("결승", "決勝");
        addTournamentTerm("준결승전", "準決勝戦");
        addTournamentTerm("준결승", "準決勝");
        addTournamentTerm("본선", "本戦");
        addTournamentTerm("예선", "予選");
        addTournamentTerm("바둑", "囲碁");
        addTournamentTerm("국제바둑대회", "国際囲碁大会");
        addTournamentTerm("선수권", "選手権");
        addTournamentTerm("최강전", "最強戦");
        addTournamentTerm("결정전", "決定戦");
        addTournamentTerm("대항전", "対抗戦");
        addTournamentTerm("특별대국", "特別対局");
        addTournamentTerm("플레이오프", "プレーオフ");
        addTournamentTerm("챔피언결정전", "チャンピオン決定戦");
        addTournamentTerm("개막전", "開幕戦");
        addTournamentTerm("리그", "リーグ");
        addTournamentTerm("오픈", "オープン");
        addTournamentTerm("토너먼트", "トーナメント");
    }

    private static void addPlayer(String korean, String japanese) {
        PLAYER_MAP.put(korean, japanese);
    }

    private static void addTournamentTerm(String korean, String japanese) {
        TOURNAMENT_TERMS_MAP.put(korean, japanese);
    }

    /**
     * 文字列に韓国語（ハングル）が含まれているか判定
     */
    public static boolean containsKorean(String text) {
        if (text == null || text.isEmpty()) return false;
        return KOREAN_PATTERN.matcher(text).find();
    }

    /**
     * 対局結果文字列を日本語表記に翻訳・正規化
     */
    public static String translateResult(String rawResult) {
        if (rawResult == null) return "";
        String trimmed = rawResult.trim();
        if (trimmed.isEmpty()) return "";

        // すでに日本語（ひらがな・カタカナ）を含む場合はそのまま
        if (trimmed.matches(".*[\\u3040-\\u309f\\u30a0-\\u30ff].*")) {
            return trimmed;
        }

        // 1. 韓国語（ハングル）形式の結果の判定と翻訳
        String textNoSpace = trimmed.replaceAll("\\s+", "");

        // 中押し勝ち (불계승)
        if (textNoSpace.contains("흑불계승") || textNoSpace.equals("흑불계") || textNoSpace.equals("흑,불계승") || textNoSpace.equals("흑(불계승)")) {
            return "黒中押し勝ち";
        }
        if (textNoSpace.contains("백불계승") || textNoSpace.equals("백불계") || textNoSpace.equals("백,불계승") || textNoSpace.equals("백(불계승)")) {
            return "白中押し勝ち";
        }
        if (textNoSpace.equals("불계승")) {
            return "中押し勝ち";
        }

        // 時間切れ勝ち (시간승)
        if (textNoSpace.contains("흑시간승") || textNoSpace.equals("흑시간")) {
            return "黒時間切れ勝ち";
        }
        if (textNoSpace.contains("백시간승") || textNoSpace.equals("백시간")) {
            return "白時間切れ勝ち";
        }
        if (textNoSpace.equals("시간승")) {
            return "時間切れ勝ち";
        }

        // 反則勝ち (반칙승)
        if (textNoSpace.contains("흑반칙승") || textNoSpace.equals("흑반칙")) {
            return "黒反則勝ち";
        }
        if (textNoSpace.contains("백반칙승") || textNoSpace.equals("백반칙")) {
            return "白反則勝ち";
        }
        if (textNoSpace.equals("반칙승")) {
            return "反則勝ち";
        }

        // 棄権勝ち (기권승)
        if (textNoSpace.contains("흑기권승") || textNoSpace.equals("흑기권")) {
            return "黒棄権勝ち";
        }
        if (textNoSpace.contains("백기권승") || textNoSpace.equals("백기권")) {
            return "白棄権勝ち";
        }
        if (textNoSpace.equals("기권승")) {
            return "棄権勝ち";
        }

        // 半目勝ち (반집승)
        if (textNoSpace.contains("흑반집승") || textNoSpace.contains("흑반집") || textNoSpace.contains("흑0.5집승")) {
            return "黒半目勝ち";
        }
        if (textNoSpace.contains("백반집승") || textNoSpace.contains("백반집") || textNoSpace.contains("백0.5집승")) {
            return "白半目勝ち";
        }
        if (textNoSpace.equals("반집승") || textNoSpace.equals("반집")) {
            return "半目勝ち";
        }

        // 持碁 / 引き分け (무승부, 빅)
        if (textNoSpace.equals("무승부") || textNoSpace.equals("빅") || textNoSpace.equals("지고")) {
            return "持碁";
        }

        // 目数勝ち (집승 / 집반승)
        Matcher bScoreHalfMatch = Pattern.compile("흑([0-9.]+)집반승?").matcher(textNoSpace);
        if (bScoreHalfMatch.find()) {
            return "黒" + bScoreHalfMatch.group(1) + "目半勝ち";
        }
        Matcher wScoreHalfMatch = Pattern.compile("백([0-9.]+)집반승?").matcher(textNoSpace);
        if (wScoreHalfMatch.find()) {
            return "白" + wScoreHalfMatch.group(1) + "目半勝ち";
        }

        Matcher bScoreMatch = Pattern.compile("흑([0-9.]+)집승?").matcher(textNoSpace);
        if (bScoreMatch.find()) {
            return "黒" + bScoreMatch.group(1) + "目勝ち";
        }
        Matcher wScoreMatch = Pattern.compile("백([0-9.]+)집승?").matcher(textNoSpace);
        if (wScoreMatch.find()) {
            return "白" + wScoreMatch.group(1) + "目勝ち";
        }

        // 単純な 흑승 / 백승
        if (textNoSpace.equals("흑승")) {
            return "黒勝ち";
        }
        if (textNoSpace.equals("백승")) {
            return "白勝ち";
        }

        // 2. 英語形式（B+R, W+3.5 等）の判定と翻訳
        String upper = trimmed.toUpperCase();
        if ("B+R".equals(upper) || "B+RESIGN".equals(upper) || "BLACK+R".equals(upper) || "BLACK+RESIGN".equals(upper)) {
            return "黒中押し勝ち";
        }
        if ("W+R".equals(upper) || "W+RESIGN".equals(upper) || "WHITE+R".equals(upper) || "WHITE+RESIGN".equals(upper)) {
            return "白中押し勝ち";
        }
        if ("B+T".equals(upper) || "B+TIME".equals(upper)) {
            return "黒時間切れ勝ち";
        }
        if ("W+T".equals(upper) || "W+TIME".equals(upper)) {
            return "白時間切れ勝ち";
        }
        if ("B+F".equals(upper) || "B+FORFEIT".equals(upper)) {
            return "黒反則勝ち";
        }
        if ("W+F".equals(upper) || "W+FORFEIT".equals(upper)) {
            return "白反則勝ち";
        }
        if ("0".equals(upper) || "VOID".equals(upper) || "DRAW".equals(upper) || "JIGO".equals(upper)) {
            return "持碁";
        }

        Matcher bEngScoreMatch = Pattern.compile("^B\\+([0-9.]+)").matcher(upper);
        if (bEngScoreMatch.find()) {
            String score = bEngScoreMatch.group(1);
            if ("0.5".equals(score)) {
                return "黒半目勝ち";
            }
            return "黒" + score + "目勝ち";
        }

        Matcher wEngScoreMatch = Pattern.compile("^W\\+([0-9.]+)").matcher(upper);
        if (wEngScoreMatch.find()) {
            String score = wEngScoreMatch.group(1);
            if ("0.5".equals(score)) {
                return "白半目勝ち";
            }
            return "白" + score + "目勝ち";
        }

        return trimmed;
    }

    /**
     * 棋士名・段位文字列を日本語に翻訳
     */
    public static String translatePlayerName(String playerName) {
        if (playerName == null) return null;
        String text = playerName.trim();
        if (text.isEmpty() || !containsKorean(text)) {
            return text;
        }

        // 棋士名辞書マッチング（完全一致・部分一致）
        for (Map.Entry<String, String> entry : PLAYER_MAP.entrySet()) {
            if (text.contains(entry.getKey())) {
                text = text.replace(entry.getKey(), entry.getValue());
            }
        }

        // 段位辞書マッチング
        for (Map.Entry<String, String> entry : RANK_MAP.entrySet()) {
            if (text.contains(entry.getKey())) {
                text = text.replace(entry.getKey(), entry.getValue());
            }
        }

        // [0-9]+단 -> [0-9]+段 の正規表現置換
        text = text.replaceAll("(\\d+)\\s*단", "$1段");
        text = text.replaceAll("(\\d+)\\s*급", "$1級");

        return text;
    }

    /**
     * 棋戦名・イベント名を日本語に翻訳
     */
    public static String translateMatchName(String matchName) {
        if (matchName == null) return null;
        String text = matchName.trim();
        if (text.isEmpty() || !containsKorean(text)) {
            return text;
        }

        // 棋戦名・用語辞書による置換
        for (Map.Entry<String, String> entry : TOURNAMENT_TERMS_MAP.entrySet()) {
            if (text.contains(entry.getKey())) {
                text = text.replace(entry.getKey(), entry.getValue());
            }
        }

        // 棋士名が含まれる場合（例: 신진서-박정환 슈퍼매치）
        for (Map.Entry<String, String> entry : PLAYER_MAP.entrySet()) {
            if (text.contains(entry.getKey())) {
                text = text.replace(entry.getKey(), entry.getValue());
            }
        }

        // 回戦・局・ラウンド等の正規表現置換
        text = text.replaceAll("제\\s*(\\d+)\\s*회", "第$1回");
        text = text.replaceAll("(\\d+)\\s*회전", "第$1回戦");
        text = text.replaceAll("(\\d+)\\s*차전", "第$1戦");
        text = text.replaceAll("(\\d+)\\s*차\\s*예선", "$1次予選");
        text = text.replaceAll("(\\d+)\\s*강전", "$1強戦");
        text = text.replaceAll("(\\d+)\\s*강", "$1強");
        text = text.replaceAll("(\\d+)\\s*국", "第$1局");
        text = text.replaceAll("(\\d+)\\s*번기", "$1番勝負");

        // 段位表記が含まれる場合
        for (Map.Entry<String, String> entry : RANK_MAP.entrySet()) {
            if (text.contains(entry.getKey())) {
                text = text.replace(entry.getKey(), entry.getValue());
            }
        }
        text = text.replaceAll("(\\d+)\\s*단", "$1段");

        return text;
    }

    /**
     * 汎用テキスト翻訳
     */
    public static String translate(String text) {
        if (text == null) return null;
        String trimmed = text.trim();
        if (trimmed.isEmpty() || !containsKorean(trimmed)) {
            return text;
        }

        String result = translateMatchName(trimmed);
        result = translatePlayerName(result);
        result = translateResult(result);
        return result;
    }

    /**
     * KifuInfo オブジェクト内の韓国語フィールドを日本語に翻訳
     */
    public static KifuInfo translateKifuInfo(KifuInfo kifuInfo) {
        if (kifuInfo == null) return null;

        if (kifuInfo.getMatchName() != null) {
            kifuInfo.setMatchName(translateMatchName(kifuInfo.getMatchName()));
        }
        if (kifuInfo.getBlackPlayer() != null) {
            kifuInfo.setBlackPlayer(translatePlayerName(kifuInfo.getBlackPlayer()));
        }
        if (kifuInfo.getWhitePlayer() != null) {
            kifuInfo.setWhitePlayer(translatePlayerName(kifuInfo.getWhitePlayer()));
        }
        if (kifuInfo.getResult() != null) {
            kifuInfo.setResult(translateResult(kifuInfo.getResult()));
        }
        return kifuInfo;
    }
}
