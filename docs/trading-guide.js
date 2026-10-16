/* ENEND trading rules. Bilingual author-supplied alpha.19 text. */
window.ENEND_TRADING_GUIDE = {
  "version": "0.1.0-alpha.19",
  "ja": {
    "common": {
      "title": "4種の商人に共通する取引ルール",
      "body": [
        "専用GUIの不使用： 専用の管理画面は作らず、バニラの取引画面を使用する。所有者が生存し、同じ次元にいて、商人から12ブロック未満の距離を維持している間だけ利用可能。取引中は商人の移動・攻撃が停止し、所有者の持っている食料の誤食も防がれる。",
        "カタログの生成と保存： 個体のUUIDをシード値として最初のカタログを生成し、永続保存する。新規個体の初期枠数：ピリジャー12、ヴィンディケーター8、ウィッチ10、エヴォーカー8。各取引項目の使用回数も個別に保存される。",
        "補充・日替わり更新の廃止： 日替わり更新、開き直しによる再抽選、再加入での在庫補充はない。使い切った項目は売り切れのまま。古いセーブデータのカタログは既存項目と使用回数を維持しつつ、不足している新スロットだけを一度だけ追加する（古い個体に規定数より多い項目があっても削られない）。",
        "禁止商品リスト： 新規生成・旧セーブデータ双方から、spawner（スポナー）、trial_spawner（試練のスポナー）、および netherite_ で始まる装備・道具は一律で除外・削除される。素材である netherite_ingot、netherite_scrap、netherite_block は禁止対象外（ただし全商人が必ず新規販売するわけではない）。",
        "価格と経験値： 価格変動係数は 0、取引経験値は 1、商人レベルの進行バーは非表示。取引成功時の音声は村人の声ではなく、商人自身の声が再生される。"
      ]
    },
    "pillager": {
      "title": "ピリジャーの取引カタログ（全12項目 ＝ 販売6枠 ＋ 買い取り6枠）",
      "body": [
        "販売6枠（各最大1回・補充なし）： 各枠で独立して10段階の価格・レアリティランクを抽選。すでに他の販売枠にある出力アイテムは重複を避けるが、該当ランクの全候補が既出の場合は全体から選んで重複を許容する（ランクの引き直しはしない）。",
        "ランク1（55% / エメラルド2〜4個 → 8個）： 丸石、砂、オークの原木、石、トウヒの原木、シラカバの原木。",
        "ランク2（25% / エメラルド5〜8個 → 4個）： 石炭、銅インゴット、鉄の原石、木炭、銅の原石、火打石。",
        "ランク3（10% / エメラルド9〜14個 → 4個）： 鉄インゴット、レッドストーン、ラピスラズリ、アメジストの欠片、グロウストーンダスト、火薬。",
        "ランク4（5% / エメラルド16〜24個 → 2個）： 金インゴット、エンダーパール、ブレイズパウダー。",
        "ランク5（2.5% / エメラルドブロック3〜5個 → 1個）： ブレイズロッド、ガストの涙、黒曜石。",
        "ランク6（1.2% / エメラルドブロック6〜9個 → 1個）： ダイヤ、泣く黒曜石、金ブロック。",
        "ランク7（0.7% / エメラルドブロック10〜16個 → 1個）： トライデント、シュルカーの殻、古代の残骸。",
        "ランク8（0.35% / エメラルドブロック20〜28個 → 1個）： ネザライトの欠片、エンチャントされた金のリンゴ。",
        "ランク9（0.2% / 1個）： ネザライトインゴット（ダイヤ3〜6個払い）またはネザースター（ダイヤ24〜40個払い）。",
        "ランク10（0.05% / ダイヤ48〜64個 → エリトラ1個）： ランク8〜10を合わせて1枠あたり約0.6%。新規ピリジャーの6枠にエリトラが1枠以上入る理論確率は約0.299%.",
        "買い取り6枠（各1回限り）：",
        "食料16個 → エメラルド3個（パン、ニンジン、ステーキ、リンゴ、ジャガイモ、焼き鳥の6種から個体ごとに均等に1種を決定）。",
        "通常の矢32本 → エメラルド5個。",
        "クロスボウ1個 → エメラルド9個。",
        "骨16個 → エメラルド3個。",
        "糸24個 → エメラルド3個。",
        "鉄の原石8個 → エメラルド4個。"
      ]
    },
    "vindicator": {
      "title": "ヴィンディケーターの取引カタログ（全8項目 ＝ 販売7枠 ＋ 買い取り1枠）",
      "body": [
        "※仲間のピリジャーによる一時友好中のみ利用可能（戦闘援護・追尾なし）。 工具は素材から均等抽選（各1個、各項目最大12回）。商品IDが diamond_ で始まる場合のみエメラルドブロック支払いとなり、それ以外はエメラルド支払い。ネザライト製品および26.3の槍は対象外。",
        "第1枠（剣 / 支払8〜19個）： 木・石・銅・鉄・金・ダイヤの剣のいずれか1種。",
        "第2枠（斧 / 支払8〜19個）： 木・石・銅・鉄・金・ダイヤの斧のいずれか1種。",
        "第3枠（ツルハシ / 支払6〜17個）： 木・石・銅・鉄・金・ダイヤのツルハシのいずれか1種。",
        "第4枠（シャベル / 支払4〜13個）： 木・石・銅・鉄・金・ダイヤのシャベルのいずれか1種。",
        "第5枠（クワ / 支払4〜13個）： 木・石・銅・鉄・金・ダイヤのクワのいずれか1種。",
        "第6・7枠（防具2枠 / 支払各6〜24個）： 革、チェーン、銅、鉄、金、ダイヤの各部位（ヘルメット、胸当て、レギンス、ブーツ）の全24候補から異なる2商品を均等選出。",
        "第8枠（買い取り / 最大20回）： 鉄インゴット12個 → エメラルド3個。"
      ]
    },
    "witch": {
      "title": "ウィッチの取引カタログ（全10項目 ＝ ポーション5枠 ＋ 材料2枠 ＋ 息吹1枠 ＋ 買い取り2枠）",
      "body": [
        "※仲間のピリジャーによる一時友好中のみ利用可能。",
        "ポーション販売5枠（各1本、エメラルドブロック3〜11個、各最大12回）： 19種の効果（healing, strength, swiftness, poison, regeneration, harming, fire_resistance, water_breathing, night_vision, invisibility, leaping, slow_falling, turtle_master, weakness, slowness, wind_charged, weaving, oozing, infested) から異なる5種を均等に選び、それぞれ1/3の確率で通常・スプラッシュ・残留のいずれかを決定（延長版・強化版の個別抽選なし）。",
        "調合材料販売2枠（各4個、エメラルド4〜16個、各最大12回）： 16種の材料（ネザーウォート、ブレイズパウダー、グロウストーンダスト、レッドストーン、砂糖、ウサギの足、クモの目、発酵したクモの目、きらめくスイカの薄切り、金のニンジン、マグマクリーム、フグ、ファントムの皮膜、カメのウロコ、ブリーズロッド、クモの巣）から異なる2種を均等選出。",
        "固定販売・買い取り枠：",
        "ドラゴンブレス（販売）： 1個、エメラルドブロック16〜35個、最大12回。",
        "ネザーウォート（買い取り）： 12個 → エメラルド4個、最大20回。",
        "ガラス瓶（買い取り）： 16個 → エメラルド2個、最大20回。"
      ]
    },
    "evoker": {
      "title": "エヴォーカーの取引カタログ（重複なし全8項目）",
      "body": [
        "※仲間のピリジャーによる一時友好中のみ利用可能。 8枠それぞれでカテゴリを抽選し、未選択の商品から均等選出。カテゴリが枯渇した場合は残りの全カテゴリから選ぶ。エメラルド系は一切使わず、14種の通貨（ダイヤ、ダイヤブロック、金・鉄・ラピス・レッドストーン・石炭・未酸化銅ブロック、金・鉄・銅の原石ブロック、およびダイヤ・金・鉄の鉱石ブロック）のいずれかで支払う。",
        "カテゴリ抽選重み・ランク・上限：",
        "物資20種（40% / ランク1 / 各4回）： 不死のトーテム、Lv30エンチャント本、不吉な瓶、エンチャントの瓶、ドラゴンブレス、エンダーパール、ブレイズロッド、ガストの涙、シュルカーの殻、エンダーチェスト、名札、鞍、オウムガイの殻、海洋の心、スポンジ、残響の欠片、ブリーズロッド、ウィンドチャージ、スカルクカタリスト、コンジット。",
        "スポーンエッグ56種（30% / ランク2 / 各4回）： 56種のモブのエッグ。",
        "試練の鍵2種（10% / ランク2 / 各4回）： 試練の鍵、不吉な試練の鍵。",
        "モブの頭6種（15% / ランク2 / 各4回）： ゾンビ、スケルトン、ウィザースケルトン、クリーパー、ドラゴン、ピグリンの頭。",
        "希少品8種（2% / ランク4 / 各1回）： エリトラ、ネザースター、エンチャントされた金のリンゴ、ヘビーコア、メイス、トライデント、ネザライトインゴット、ネザライトの欠片。",
        "クリエイティブ系6種（3% / ランク4 / 各1回）： 芽生えたアメジスト、強化された深層岩、石化したオークのハーフブロック、プレイヤーの頭、カエルの卵、コーラスプラント。",
        "価格の目安： ダイヤ払いの場合はランク1=8〜16個、ランク2=16〜24個、ランク4=32〜40個（ダイヤブロック払いは1〜3個 / 2〜4個 / 4〜6個、その他12通貨は16〜32個 / 24〜40個 / 40〜56個）。唯一の例外としてネザライトインゴット1個は必ずダイヤ3〜6個（最大1回）。"
      ]
    }
  },
  "en": {
    "common": {
      "title": "General Trading Rules (All 4 Merchant Types)",
      "body": [
        "Vanilla GUI Integration: No custom management screens. Trading uses the standard vanilla trade GUI, accessible only while the owner is alive, in the same dimension, and within 12 blocks of the merchant. Opening the trade screen freezes the merchant's movement and attacks, and stops the owner from accidentally eating held food.",
        "Catalog Generation & Persistence: Catalogs are generated on first spawn using the mob's UUID as a seed and saved persistently. Initial item slots per merchant: Pillager: 12, Vindicator: 8, Witch: 10, Evoker: 8. Usage counts for every trade slot are saved.",
        "No Restocks: There are no daily refreshes, re-opening re-rolls, or restocks upon re-joining. Once a slot's trade uses are exhausted, it stays sold out. Older saved catalogs preserve existing items and usage counts while appending any newly added slots once; if an older mob has more slots than current rules, it is not trimmed down.",
        "Blacklisted Items: Excluded entirely from new generation and scrubbed from old saves: spawner, trial_spawner, and any equipment/tools starting with netherite_. Raw materials like netherite_ingot, netherite_scrap, and netherite_block are not blacklisted, though it doesn't mean every merchant sells them.",
        "Pricing & Experience: Price fluctuation multipliers are set to 0, trade experience is 1, and the merchant level progress bar is hidden. Successful trade sounds use the merchant's own voice, never a villager's."
      ]
    },
    "pillager": {
      "title": "Pillager Merchant Catalog (12 Slots = 6 Buy + 6 Sell)",
      "body": [
        "6 Sell Slots: Each slot independently rolls one of 10 rarity tiers. Tiers pick candidates uniformly, avoiding duplicate output items already present in other sell slots (unless all candidates in that tier are already used, allowing duplicates without re-rolling tiers). Max 1 use per slot (never restocks).",
        "Tier 1 (55% / 2–4 Emeralds → 8 Items): Cobblestone, Sand, Oak Log, Stone, Spruce Log, Birch Log.",
        "Tier 2 (25% / 5–8 Emeralds → 4 Items): Coal, Copper Ingot, Raw Iron, Charcoal, Raw Copper, Flint.",
        "Tier 3 (10% / 9–14 Emeralds → 4 Items): Iron Ingot, Redstone, Lapis Lazuli, Amethyst Shard, Glowstone Dust, Gunpowder.",
        "Tier 4 (5% / 16–24 Emeralds → 2 Items): Gold Ingot, Ender Pearl, Blaze Powder.",
        "Tier 5 (2.5% / 3–5 Emerald Blocks → 1 Item): Blaze Rod, Ghast Tear, Obsidian.",
        "Tier 6 (1.2% / 6–9 Emerald Blocks → 1 Item): Diamond, Crying Obsidian, Gold Block.",
        "Tier 7 (0.7% / 10–16 Emerald Blocks → 1 Item): Trident, Shulker Shell, Ancient Debris.",
        "Tier 8 (0.35% / 20–28 Emerald Blocks → 1 Item): Netherite Scrap, Enchanted Golden Apple.",
        "Tier 9 (0.2% / 1 Item): Netherite Ingot (costs 3–6 Diamonds) or Nether Star (costs 24–40 Diamonds).",
        "Tier 10 (0.05% / 48–64 Diamonds → 1 Elytra): Combined Tiers 8–10 give a ~0.6% chance per slot. The theoretical chance of rolling at least one Elytra across all 6 sell slots on a fresh Pillager is",
        "1−0.9995",
        "6",
        "≈0.299625%.",
        "6 Buy Slots (1 use each):",
        "16 Foods → 3 Emeralds (uniformly picks 1 of 6 types per mob: Bread, Carrot, Cooked Beef, Apple, Potato, or Cooked Chicken).",
        "32 Arrows → 5 Emeralds.",
        "1 Crossbow → 9 Emeralds.",
        "16 Bones → 3 Emeralds.",
        "24 String → 3 Emeralds.",
        "8 Raw Iron → 4 Emeralds."
      ]
    },
    "vindicator": {
      "title": "Vindicator Merchant Catalog (8 Slots = 7 Sell + 1 Buy)",
      "body": [
        "Note: Requires temporary truce via a nearby tamed Pillager; no combat support or following. Tools uniformly pick from material sets (1 item per trade, up to 12 uses per slot). Diamond-tier tools/armor cost Emerald Blocks, while all other tiers cost Emeralds. Netherite gear and 26.3 Spears are excluded.",
        "Slot 1 (Swords / Cost 8–19): Wooden, Stone, Copper, Iron, Golden, or Diamond Sword.",
        "Slot 2 (Axes / Cost 8–19): Wooden, Stone, Copper, Iron, Golden, or Diamond Axe.",
        "Slot 3 (Pickaxes / Cost 6–17): Wooden, Stone, Copper, Iron, Golden, or Diamond Pickaxe.",
        "Slot 4 (Shovels / Cost 4–13): Wooden, Stone, Copper, Iron, Golden, or Diamond Shovel.",
        "Slot 5 (Hoes / Cost 4–13): Wooden, Stone, Copper, Iron, Golden, or Diamond Hoe.",
        "Slots 6 & 7 (Armor / 2 slots, Cost 6–24): Uniformly picks 2 distinct pieces from 24 candidates (Leather, Chainmail, Copper, Iron, Golden, Diamond across Helmet, Chestplate, Leggings, Boots).",
        "Slot 8 (Buy / Max 20 uses): 12 Iron Ingots → 3 Emeralds."
      ]
    },
    "witch": {
      "title": "Witch Merchant Catalog (10 Slots = 5 Potions + 2 Ingredients + 1 Breath + 2 Buy)",
      "body": [
        "Note: Requires temporary truce via a nearby tamed Pillager.",
        "5 Potion Slots (1 bottle each, 3–11 Emerald Blocks, max 12 uses each): Uniformly picks 5 distinct effects from 19 potions (healing, strength, swiftness, poison, regeneration, harming, fire_resistance, water_breathing, night_vision, invisibility, leaping, slow_falling, turtle_master, weakness, slowness, wind_charged, weaving, oozing, infested), then rolls a 1/3 chance for normal, splash, or lingering variants. No tier splits (extended/strong) are rolled separately.",
        "2 Ingredient Slots (4 items each, 4–16 Emeralds, max 12 uses each): Uniformly picks 2 distinct reagents from 16 choices (nether_wart, blaze_powder, glowstone_dust, redstone, sugar, rabbit_foot, spider_eye, fermented_spider_eye, glistering_melon_slice, golden_carrot, magma_cream, pufferfish, phantom_membrane, turtle_scute, breeze_rod, cobweb).",
        "Fixed Sales & Buy Slots:",
        "Dragon's Breath (Sell): 1 bottle for 16–35 Emerald Blocks, max 12 uses.",
        "Nether Wart (Buy): 12 wart → 4 Emeralds, max 20 uses.",
        "Glass Bottles (Buy): 16 bottles → 2 Emeralds, max 20 uses."
      ]
    },
    "evoker": {
      "title": "Evoker Merchant Catalog (8 Non-Duplicate Slots)",
      "body": [
        "Note: Requires temporary truce via a nearby tamed Pillager. Each of the 8 slots rolls a category, picking a unique item from it. If a category is exhausted, it picks uniformly from all remaining categories (without re-rolling category weights).",
        "Categories, Weights, Ranks & Limits:",
        "Supplies (20 items): 40% weight / Price Rank 1 / Max 4 uses each.",
        "Spawn Eggs (56 items): 30% weight / Price Rank 2 / Max 4 uses each.",
        "Trial Keys (2 items): 10% weight / Price Rank 2 / Max 4 uses each.",
        "Mob Heads (6 items): 15% weight / Price Rank 2 / Max 4 uses each.",
        "Rare Items (8 items): 2% weight / Price Rank 4 / Max 1 use each.",
        "Creative-tier Blocks (6 items): 3% weight / Price Rank 4 / Max 1 use each.",
        "Currencies (No Emeralds): Uniformly picks 1 of 14 currency types (diamond, diamond_block, gold_block, iron_block, lapis_block, redstone_block, coal_block, copper_block, raw_gold_block, raw_iron_block, raw_copper_block, diamond_ore, gold_ore, iron_ore).",
        "Pricing: Diamonds: R1=8–16, R2=16–24, R4=32–40. Diamond Blocks: R1=1–3, R2=2–4, R4=4–6. Other 12 currencies: R1=16–32, R2=24–40, R4=40–56. (Exception: Netherite Ingot always costs 3–6 Diamonds, max 1 use).",
        "Output Quantities & Mechanics: Experience Bottles & Wind Charges drop in stacks of 8; Ender Pearls & Dragon's Breath in stacks of 4; all others 1. Books (book) are rolled as Level-30 enchanted books via vanilla mechanics. Spawn eggs cover 56 standard passive/hostile mobs (_spawn_egg). Creative-tier blocks cover exactly the specified list (Budding Amethyst, Reinforced Deepslate, Petrified Oak Slab, Player Head, Frogspawn, Chorus Plant). Older saves with emerald prices have their currency slots converted to the 14 new currencies while preserving use limits."
      ]
    }
  }
};
