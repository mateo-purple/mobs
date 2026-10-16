/* ENEND Official Mobdex. All creature facts originate in data.js (alpha.19). */
(() => {
  'use strict';
  const D = window.ENEND_DATA;
  if (!D || !Array.isArray(D.mobs)) return;
  const $ = id => document.getElementById(id);
  const esc = value => String(value ?? '').replace(/[&<>"']/g, ch => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[ch]));
  const storage = {
    get(key, fallback) { try { return localStorage.getItem(key) ?? fallback; } catch (_) { return fallback; } },
    set(key, value) { try { localStorage.setItem(key, value); } catch (_) {} }
  };
  const t = {
    ja: {
      skip:'本文へ移動',navDex:'Mob図鑑',navCommands:'遊び方',navTrade:'取引',navReference:'資料',
      heroEyebrow:'THE OFFICIAL MONSTER FIELD GUIDE',heroTitle1:'敵の中に、',heroTitle2:'味方がいる。',
      heroLead:'必ずしも、すべてのモンスターが敵とは限らない。アイテムを贈って信頼を得れば、かつて恐れた相手が旅を共にする仲間になるかもしれません。',
      heroExplore:'仲間を探す',heroLearn:'遊び方を見る',metricKinds:'関連するMob',metricFriends:'直接仲間化',metricTruce:'間接加入・停戦',
      dexTitle:'モンスター図鑑',dexLead:'まずは身近な4種から。残りの仲間は、あなた自身の旅で見つけてください。',
      searchMob:'Mobを検索',searchPlaceholder:'名前・素材・能力で検索',nothingTitle:'見つかりませんでした',nothingLead:'別の名前やカテゴリを試してください。',
      commandTitle:'手ぶらで、指示を出そう。',commandLead:'専用の管理画面は不要。Minecraftの操作だけで仲間と行動できます。',
      followTitle:'追尾',followBody:'プレイヤーについてきて、戦闘では味方として援護します。離れすぎると帰還を試みます。',
      stayTitle:'待機',stayBody:'その場で停止し、勝手な索敵や援護をやめます。基地で待っていてもらいたいときに。',
      patrolTitle:'巡回',patrolBody:'指示地点の周辺を動き、近くの敵と自動で交戦します。拠点の警備に。',
      howSwitchTitle:'素手で右クリックして切り替え',howSwitchBody:'取引や騎乗のあるMobは、スニーク＋素手右クリックで命令します。一部のMobには専用の行動ルールがあります。',
      hornTitle:'角笛による一斉命令（詳細）',bannerEyebrow:'THERE IS MORE TO DISCOVER',bannerTitle:'敵対だけが、答えじゃない。',
      bannerCopy:'ある相手とは戦い、ある相手とは交渉する。すべてのMobが同じ仲間になり方をするわけではありません。',
      tradeTitle:'危うい停戦。意外な取引。',tradeLead:'ピリジャーを仲間にすると、近くの邪悪な村人との取引への道が開けます。誰か一人でも攻撃すれば停戦は終わりです。',
      tradeSpotlight:'取引はバニラの画面を使用',tradeSpotlightBody:'4種類の商人には異なる商品があります。実際の内容と価格は各個体の保存された在庫に基づきます。',
      tradeNoReset:'alpha.19：日替わり再抽選なし',fullTradeTitle:'商品の全候補・価格・取引上限（ネタバレ）',
      referenceTitle:'資料と更新情報',referenceLead:'この図鑑はMinecraft 26.3対応のalpha.19仕様書を基にしています。数値は初期設定で、後の更新で変更される場合があります。',
      refSpecs:'共通仕様・保存・戦闘',refSpecsBody:'仲間のHP、装備、味方への攻撃、死亡・保存などの仕様。',
      refSettings:'設定値・変更点',refSettingsBody:'仲間化率、距離、待機時間などの調整と旧仕様との差分。',
      refExcluded:'対象外のMob',refExcludedBody:'仲間化・停戦に対応しない敵の一覧。',viewDetails:'詳しく見る →',
      sourceText:'仕様はalpha.19の全35種照合文書を参照しています。コード上の実装と理想仕様が異なる箇所は原文の注意書きを優先してください。',
      downloadRaw:'日本語の全仕様書を開く ↗',footerDisclaimer:'Minecraftファン制作MODのガイドです。Mojang / Microsoftとは無関係です。',
      all:'すべて',frontline:'前衛',ranged:'遠距離',utility:'支援',diplomacy:'交渉',boss:'強敵',
      spoilerOn:'全図鑑を表示中',spoilerOff:'ネタバレ保護中',onExplain:'35種の条件・能力を閲覧できます',offExplain:'秘密の仲間と入手条件は非表示です',showAll:'全35種を表示',hideAll:'ネタバレを隠す',
      hiddenTitle:'あと31種の出会いが待っています。',hiddenCopy:'ネタバレを表示すると、仲間にする条件・成功率・HP・固有能力まで閲覧できます。',unlock:'全図鑑を解禁',
      viewFile:'詳しい仕様を読む',rate:'成功率',item:'仲間にする方法',hp:'最大HP',record:'技術仕様・注意点（alpha.19原文）',
      localeNote:'技術的な詳細は日本語の原文を表示しています。掲載情報はalpha.19の仕様に基づきます。',
      indirect:'間接加入・停戦',next:'次のMob',previous:'前のMob',share:'リンクをコピー',copied:'コピーしました',
      spoilerRequired:'この項目には未公開の仲間が含まれます。全図鑑のネタバレを有効にすると表示できます。',
      sourceVersion:'資料対象：0.1.0-alpha.19 / Java 25 / Minecraft 26.3 / NeoForge 26.3.0.83',
      vendorPillager:'周辺の友好化を仲介。珍しい物資の販売や買取を行います。',vendorVindicator:'武器・防具・道具を取り扱います。',vendorWitch:'ポーションや醸造素材を販売します。',vendorEvoker:'稀少品と高価な特殊アイテムを扱います。',
      noReplenish:'在庫は再入荷しません',detailClose:'閉じる',guideSource:'全仕様書の原文に基づく',
      release:'対応版'
    },
    en: {
      skip:'Skip to content',navDex:'Mobdex',navCommands:'How to Play',navTrade:'Trading',navReference:'Reference',
      heroEyebrow:'THE OFFICIAL MONSTER FIELD GUIDE',heroTitle1:'Your enemy,',heroTitle2:'your next friend.',
      heroLead:'Not every monster wants to be your enemy. Earn their trust with gifts and those you once feared may become your most unexpected companions.',
      heroExplore:'Explore the Mobdex',heroLearn:'How to Play',metricKinds:'Featured creature types',metricFriends:'Direct recruits',metricTruce:'Indirect / truce',
      dexTitle:'Monster Encyclopedia',dexLead:'Meet four familiar faces first. Find the rest of your companions on your own journey.',
      searchMob:'Search mobs',searchPlaceholder:'Search name, item, ability…',nothingTitle:'No matches found',nothingLead:'Try a different name or category.',
      commandTitle:'No menus. Just commands.',commandLead:'Interact with companions using familiar Minecraft controls.',
      followTitle:'Follow',followBody:'Your companion travels with you and supports you in combat, returning if it falls too far behind.',
      stayTitle:'Stay',stayBody:'Your companion holds its position and stops seeking out or attacking enemies on its own.',
      patrolTitle:'Patrol',patrolBody:'Your companion explores its assigned area and automatically engages nearby enemies.',
      howSwitchTitle:'Empty-hand right-click to cycle commands',howSwitchBody:'For mobs with trading or riding interactions, use sneak + empty-hand right-click instead. Some companions have special rules.',
      hornTitle:'Group commands using goat horns (technical details)',bannerEyebrow:'THERE IS MORE TO DISCOVER',bannerTitle:'Fighting is not the only option.',
      bannerCopy:'Some creatures fight beside you; others negotiate. No two friendships have to look alike.',
      tradeTitle:'Fragile peace. Unlikely trades.',tradeLead:'Befriend a Pillager and nearby Illagers may agree to trade. Attack even one of them and the truce comes to an end.',
      tradeSpotlight:'Uses the vanilla trading interface',tradeSpotlightBody:'Four kinds of traders offer different inventories, with stock stored per individual.',
      tradeNoReset:'Alpha .19: no daily trade rerolls',fullTradeTitle:'All trade candidates, prices and stock (spoilers)',
      referenceTitle:'Field Notes & Reference',referenceLead:'Based on the alpha.19 specification for Minecraft 26.3. Figures are defaults and may change in later releases.',
      refSpecs:'Common Rules & Combat',refSpecsBody:'Health, equipment, friendly fire, deaths and persistence.',
      refSettings:'Configuration & Changes',refSettingsBody:'Rates, ranges, timers and differences from older builds.',
      refExcluded:'Excluded Mobs',refExcludedBody:'Creatures without new ENEND recruitment or truce methods.',viewDetails:'View details →',
      sourceText:'Derived from the alpha.19 specification covering all 35 supported species. Where the planned and implemented behaviors differ, defer to the source notes.',
      downloadRaw:'View the full Japanese specification ↗',footerDisclaimer:'Unofficial fan-made Minecraft mod guide. Not affiliated with Mojang or Microsoft.',
      all:'All',frontline:'Melee',ranged:'Ranged',utility:'Utility',diplomacy:'Diplomacy',boss:'Boss',
      spoilerOn:'Full Mobdex unlocked',spoilerOff:'Spoiler protection is on',onExplain:'All 35 entries and their requirements are visible',offExplain:'Secret companions and requirements stay hidden',showAll:'Reveal all 35',hideAll:'Hide spoilers',
      hiddenTitle:'31 more discoveries await.',hiddenCopy:'Reveal the full Mobdex to explore requirements, chances, health, and individual abilities.',unlock:'Reveal the Mobdex',
      viewFile:'Read full specs',rate:'Recruit chance',item:'How to befriend',hp:'Max health',record:'Technical notes (original alpha.19 specification)',
      localeNote:'Detailed technical descriptions are preserved in Japanese to avoid changing exact behavior. Based on alpha.19.',
      indirect:'Indirect / truce',next:'Next mob',previous:'Previous mob',share:'Copy link',copied:'Copied!',
      spoilerRequired:'This section contains unrevealed creatures. Unlock the full Mobdex to view it.',
      sourceVersion:'Specification: 0.1.0-alpha.19 / Java 25 / Minecraft 26.3 / NeoForge 26.3.0.83',
      vendorPillager:'Broker of ceasefires. Sells and buys unusual supplies.',vendorVindicator:'Trades in weapons, tools and armor.',vendorWitch:'Offers potions and brewing materials.',vendorEvoker:'Deals in rare and expensive curiosities.',
      noReplenish:'Stock does not restock',detailClose:'Close',guideSource:'Sourced from full implementation specification',release:'Compatibility'
    }
  };
  const categoryColors = {frontline:'#2a4e32',ranged:'#365344',utility:'#305050',diplomacy:'#495039',boss:'#50343d'};
  const merchantData = [
    {id:'pillager',icon:'⚑',stock:12}, {id:'vindicator',icon:'⚒',stock:8},
    {id:'witch',icon:'⚗',stock:10}, {id:'evoker',icon:'✧',stock:8}
  ];
  const categories = ['all','frontline','ranged','utility','diplomacy','boss'];
  const methodsEn={
    creeper:'TNT',zombie:'Raw meat',zombie_villager:'Raw meat',husk:'Raw meat',drowned:'Raw meat',zombified_piglin:'Raw meat',
    skeleton:'Plain arrows',stray:'Plain arrows',bogged:'Plain arrows',parched:'Plain arrows',wither_skeleton:'Wither rose',
    spider:'Raw meat',cave_spider:'Raw meat',slime:'Slime block',magma_cube:'Lava bucket',blaze:'Blaze powder',ghast:'Snowball',
    enderman:'Eye of Ender (eye contact)',pillager:'Crossbow / plain arrow / cooked food',
    vindicator:'Truce via recruited Pillager',witch:'Truce via recruited Pillager',evoker:'Truce via recruited Pillager',
    piglin:'Raw/cooked pork or golden armor (wear gold armor)',piglin_brute:'Golden axe (truce only)',hoglin:'Mushroom blocks',
    phantom:'Raw meat',guardian:'Joins with Elder Guardian',elder_guardian:'Gold block',shulker:'End rod',silverfish:'Food',
    endermite:'Ender pearl',warden:'Sculk catalyst (HP ≤ 50)',wither:'Wither skeleton skull / Nether star (HP ≤ 30)',
    ender_dragon:'Dragon egg / End crystal (after first kill)',ravager:'Hay bale'
  };
  const featuredIds = ['creeper','zombie','skeleton','pillager'];
  const savedLang = storage.get('enend.lang','ja');
  let language = savedLang==='en'?'en':'ja';
  let spoilers = storage.get('enend.spoilers','off')==='on';
  let filter = 'all';
  let query = '';
  let selectedMob = null;
  const txt = key => t[language][key] ?? key;
  const mobName = mob => language==='ja'?mob.nameJa:mob.nameEn;

  function svgFor(kind){
    const box = (fill, facial) => `<svg class="mob-sprite mob-pixel" viewBox="0 0 80 80" role="img" aria-label="${esc(kind)}"><rect x="9" y="6" width="62" height="63" fill="${fill}"/><rect x="9" y="6" width="62" height="10" fill="#ffffff1c"/>${facial}</svg>`;
    if(kind==='creeper') return box('#5cce7a','<rect x="18" y="25" width="16" height="18" fill="#10281d"/><rect x="47" y="25" width="16" height="18" fill="#10281d"/><rect x="34" y="40" width="13" height="17" fill="#10281d"/><rect x="25" y="52" width="30" height="14" fill="#10281d"/>');
    if(kind==='zombie' || kind==='zombie_villager')return box('#78b889','<rect x="17" y="28" width="18" height="8" fill="#21331f"/><rect x="48" y="28" width="14" height="8" fill="#21331f"/><rect x="22" y="48" width="40" height="11" fill="#27412e"/><rect x="36" y="48" width="9" height="8" fill="#a8a1a0"/>');
    if(['skeleton','stray','bogged','parched','wither_skeleton'].includes(kind))return box(kind==='wither_skeleton'?'#535962':'#dedfdb','<rect x="20" y="27" width="15" height="18" fill="#1c2421"/><rect x="47" y="27" width="15" height="18" fill="#1c2421"/><rect x="36" y="49" width="10" height="8" fill="#1c2421"/><rect x="26" y="61" width="29" height="5" fill="#1c2421"/>');
    if(['pillager','vindicator','evoker','witch'].includes(kind))return box('#a1a9a0','<rect x="17" y="26" width="17" height="9" fill="#2e3128"/><rect x="49" y="26" width="15" height="9" fill="#2e3128"/><rect x="36" y="37" width="13" height="20" fill="#677066"/><rect x="23" y="59" width="35" height="6" fill="#4d574c"/>');
    const mapping={spider:'✳',cave_spider:'✳',slime:'◕',magma_cube:'◆',blaze:'✹',ghast:'☷',enderman:'⬒',
      piglin:'♜',piglin_brute:'♜',hoglin:'♟',phantom:'⌁',guardian:'◈',elder_guardian:'◈',shulker:'◉',
      silverfish:'〰',endermite:'⌁',warden:'♞',wither:'✴',ender_dragon:'✥',ravager:'♟'};
    const mark=mapping[kind]??'◆';
    return `<svg class="mob-sprite" viewBox="0 0 80 80" role="img" aria-label="${esc(kind)}"><rect x="7" y="7" width="66" height="66" rx="10" fill="#10291d" stroke="#80c99a" stroke-width="2"/><path d="M11 21h57M11 40h57M11 59h57M25 10v61M43 10v61M61 10v61" stroke="#376b48" stroke-width="1" opacity=".55"/><text x="40" y="52" text-anchor="middle" font-family="sans-serif" font-size="43" fill="#b3f5c4">${mark}</text></svg>`;
  }

  function applyLanguage(){
    document.documentElement.lang=language;
    document.querySelectorAll('[data-i18n]').forEach(el=>{el.textContent=txt(el.dataset.i18n);});
    $('langToggle').innerHTML=language==='ja'?'EN <span class="sep">/</span> <strong>JP</strong>':'<strong>EN</strong> <span class="sep">/</span> JP';
    $('mobSearch').placeholder=txt('searchPlaceholder');
    $('spoilerToggleLabel').textContent=spoilers?txt('hideAll'):txt('showAll');
    $('discoverLabel').textContent=txt(spoilers?'spoilerOn':'spoilerOff');
    $('discoverDescription').textContent=txt(spoilers?'onExplain':'offExplain');
    $('mysteryTitle').textContent=txt('hiddenTitle');
    $('mysteryCopy').textContent=txt('hiddenCopy');
    $('unlockButton').firstChild.textContent=txt('unlock')+' ';
    renderFilters();renderMobs();renderMerchants();renderReference();
    if(selectedMob && $('detailDialog').open) openMob(selectedMob,false);
  }
  function setSpoilers(value){
    spoilers=value;
    storage.set('enend.spoilers',value?'on':'off');
    $('spoilerToggle').checked=value;
    if(!value && selectedMob && !featuredIds.includes(selectedMob.id)){
      $('detailDialog').close();selectedMob=null;
    }
    applyLanguage();
  }
  function renderFilters(){
    $('categoryFilters').innerHTML='';
    for(const id of categories){
      const btn=document.createElement('button');btn.type='button';btn.className='filter-button'+(id===filter?' active':'');
      btn.textContent=txt(id);btn.setAttribute('aria-pressed',String(id===filter));
      btn.addEventListener('click',()=>{filter=id;renderFilters();renderMobs();});
      $('categoryFilters').append(btn);
    }
  }
  function renderMobs(){
    let available=spoilers?D.mobs:featuredIds.map(id=>D.mobs.find(m=>m.id===id));
    available=available.filter(m=>filter==='all'||m.category===filter);
    if(query.trim()){
      const q=query.trim().toLocaleLowerCase();
      available=available.filter(m=>[m.id,m.nameJa,m.nameEn,m.method,m.chance,m.summaryJa,m.summaryEn,methodsEn[m.id]]
        .filter(Boolean).some(a=>String(a).toLocaleLowerCase().includes(q)));
    }
    const grid=$('mobGrid');grid.replaceChildren();
    for(const mob of available){
      const article=document.createElement('article');article.className='mob-card';
      article.style.setProperty('--tone',categoryColors[mob.category]||'#304a3b');
      article.innerHTML=`<div class="mob-image"><span class="card-number">${String(mob.index).padStart(2,'0')}</span><span class="card-cat">${esc(txt(mob.category).toUpperCase())}</span>${svgFor(mob.id)}</div>
         <div class="card-content"><h3>${esc(mobName(mob))}</h3><div class="mob-localized">${esc(language==='ja'?mob.nameEn:mob.nameJa)}</div><p class="card-summary">${esc(language==='ja'?mob.summaryJa:mob.summaryEn)}</p>
         <div class="card-bottom"><span class="chance">${esc(mob.chance)}</span><span class="card-more">${esc(txt('viewFile'))} ↗</span></div></div>`;
      const action=document.createElement('button');action.className='card-click';action.type='button';
      action.setAttribute('aria-label',`${txt('viewFile')}: ${mobName(mob)}`);
      action.addEventListener('click',()=>openMob(mob,true));article.append(action);grid.append(article);
    }
    $('emptyState').hidden=available.length>0;
    $('mysteryPanel').hidden=spoilers;
    // Explicit style, since our responsive flex rule otherwise applies when hidden.
    $('mysteryPanel').style.display=spoilers?'none':'';
  }
  function openMob(mob,moveHash){
    if(!spoilers && !featuredIds.includes(mob.id)){referenceWindow('excluded');return;}
    selectedMob=mob;
    const chance=language==='ja'?mob.chance:mob.chance.replace('直接仲間化なし','Indirect only').replace('仲間化なし','Truce only').replace('直接抽選なし','Indirect only');
    const method=language==='ja'?mob.method:methodsEn[mob.id]||mob.method;
    const summary=language==='ja'?mob.summaryJa:mob.summaryEn;
    const quickStats=[{label:txt('item'),value:method},{label:txt('rate'),value:chance},{label:txt('hp'),value:mob.hp||'—'}];
    const current=spoilers?D.mobs:featuredIds.map(id=>D.mobs.find(x=>x.id===id));
    const idx=current.findIndex(x=>x.id===mob.id);
    $('dialogBody').innerHTML=`<div class="dialog-content"><h2 id="dialogTitle" class="dialog-title">${esc(mobName(mob))}</h2>
      <p class="dialog-sub">${esc(language==='ja'?mob.nameEn:mob.nameJa)} &nbsp; · &nbsp; ${esc(txt(mob.category))} &nbsp; · &nbsp; ${String(mob.index).padStart(2,'0')} / 35</p>
      <div class="dialog-summary">${esc(summary)}</div>
      <div class="dialog-stats">${quickStats.map(x=>`<div class="stat"><label>${esc(x.label)}</label><strong>${esc(x.value)}</strong></div>`).join('')}</div>
      <h3 class="dialog-section-title">${esc(txt('record'))}</h3>
      ${language==='en'?`<div class="lang-hint">${esc(txt('localeNote'))}</div>`:''}
      <div class="dialog-detail">${esc(mob.detail)}</div>
      <p class="dialog-footnote">${esc(txt('sourceVersion'))}</p>
      <div class="dialog-actions"><button id="prevMob" type="button" ${idx<=0?'disabled':''}>← ${esc(txt('previous'))}</button><button id="copyMobLink" type="button">${esc(txt('share'))}</button><button id="nextMob" type="button" ${idx===current.length-1?'disabled':''}>${esc(txt('next'))} →</button></div></div>`;
    $('prevMob').addEventListener('click',()=>{if(idx>0)openMob(current[idx-1],true);});
    $('nextMob').addEventListener('click',()=>{if(idx<current.length-1)openMob(current[idx+1],true);});
    $('copyMobLink').addEventListener('click',async()=>{
      const url=location.href.split('#')[0]+'#mob-'+mob.id;
      try{await navigator.clipboard.writeText(url);}catch(_){const box=document.createElement('textarea');box.value=url;document.body.append(box);box.select();document.execCommand('copy');box.remove();}
      $('copyMobLink').textContent=txt('copied');
    });
    if(!$('detailDialog').open)$('detailDialog').showModal();
    if(moveHash)history.replaceState(null,'','#mob-'+mob.id);
  }
  function closeModal(){if($('detailDialog').open)$('detailDialog').close();selectedMob=null;}
  function tradeChunks(){
    let parts=D.trades;
    let ranges={pillager:[],vindicator:[],witch:[],evoker:[],common:[]};
    let current='common';
    for(const part of parts){
      for(const id of ['pillager','vindicator','witch','evoker']){
        const map={pillager:'ピリジャー',vindicator:'ヴィンディケーター',witch:'ウィッチ',evoker:'エヴォーカー'};
        if(part.title.includes(map[id])){current=id;break;}
      }
      ranges[current].push(part);
    }
    return ranges;
  }
  function renderMerchants(){
    const grid=$('merchantGrid');grid.replaceChildren();
    const names={pillager:'Pillager',vindicator:'Vindicator',witch:'Witch',evoker:'Evoker'};
    const jp={pillager:'ピリジャー',vindicator:'ヴィンディケーター',witch:'ウィッチ',evoker:'エヴォーカー'};
    for(const merchant of merchantData){
      const node=document.createElement('article');node.className='merchant-card';
      node.innerHTML=`<div class="merchant-icon" aria-hidden="true">${merchant.icon}</div><h3>${esc(language==='ja'?jp[merchant.id]:names[merchant.id])}</h3>
        <p>${esc(txt('vendor'+merchant.id[0].toUpperCase()+merchant.id.slice(1)))}</p>
        <div class="merchant-meta"><span>${merchant.stock} ${language==='ja'?'取引枠':'offers'}</span><span>${language==='ja'?'一時停戦':'TRUCE'}</span></div>`;
      grid.append(node);
    }
  }
  function renderReference(){
    const common=D.common.filter(x=>x.title.includes('角笛'));
    $('hornContent').innerHTML=common.map(block=>`<p class="reference-text">${esc(block.content)}</p>`).join('');
    const ranges=tradeChunks();
    let parts=[];
    for(const id of ['common','pillager','vindicator','witch','evoker']){
      parts.push(...ranges[id]);
    }
    $('tradeDetails').innerHTML=parts.map(block=>`<div class="ref-block"><h4>${esc(block.title)}</h4><p>${esc(block.content)}</p></div>`).join('');
    // Trade entries include hidden formulas and rare prizes; closed by default.
    if(!spoilers){
      $('tradeDetails').innerHTML=`<p>${esc(txt('spoilerRequired'))}</p><button class="outline-btn" id="showTradeInfo" type="button">${esc(txt('unlock'))}</button>`;
      $('showTradeInfo').addEventListener('click',()=>setSpoilers(true));
    }
  }
  function referenceWindow(kind){
    const title=kind==='common'?txt('refSpecs'):kind==='settings'?txt('refSettings'):txt('refExcluded');
    const wrap=$('referenceDialogBody');
    let body='';
    if(kind==='common'){
      body=D.common.map(p=>`<h4 class="dialog-section-title">${esc(p.title)}</h4><p>${esc(p.content)}</p>`).join('');
    } else if(kind==='settings'){
      body=`<p>${esc(D.settings)}</p>`;
    } else if(!spoilers){
      body=`<p>${esc(txt('spoilerRequired'))}</p><button id="unlockFromDialog" class="outline-btn" type="button">${esc(txt('unlock'))}</button>`;
    }else{
      body=D.excluded.map(e=>`<h4 class="dialog-section-title">${esc(e.nameJa)} (${esc(e.id)})</h4><p>${esc(e.description)}</p>`).join('');
    }
    wrap.innerHTML=`<div class="dialog-content"><h2 class="reference-dialog-title" id="refDialogTitle">${esc(title)}</h2><div class="reference-dialog-content">${body}</div><p class="dialog-footnote">${esc(txt('sourceVersion'))}</p></div>`;
    if(!spoilers && kind==='excluded')$('unlockFromDialog').addEventListener('click',()=>{setSpoilers(true);referenceWindow('excluded');});
    if(!$('referenceDialog').open)$('referenceDialog').showModal();
  }
  function handleHash(){
    if(!location.hash.startsWith('#mob-'))return;
    const id=location.hash.slice(5);const mob=D.mobs.find(x=>x.id===id);
    if(!mob)return;
    if(spoilers || featuredIds.includes(id))openMob(mob,false);
    else { // preserve an explicit direct-link spoiler choice
      referenceWindow('excluded');
    }
  }
  function init(){
    $('spoilerToggle').checked=spoilers;
    $('spoilerToggle').addEventListener('change',e=>setSpoilers(e.target.checked));
    $('unlockButton').addEventListener('click',()=>setSpoilers(true));
    $('langToggle').addEventListener('click',()=>{language=language==='ja'?'en':'ja';storage.set('enend.lang',language);applyLanguage();});
    $('mobSearch').addEventListener('input',e=>{query=e.target.value;renderMobs();});
    $('openCommon').addEventListener('click',()=>referenceWindow('common'));
    $('openSettings').addEventListener('click',()=>referenceWindow('settings'));
    $('openExcluded').addEventListener('click',()=>referenceWindow('excluded'));
    $('closeDialog').addEventListener('click',closeModal);
    $('closeReferenceDialog').addEventListener('click',()=>$('referenceDialog').close());
    $('detailDialog').addEventListener('close',()=>{selectedMob=null;});
    for(const id of ['detailDialog','referenceDialog']){
      $(id).addEventListener('click',e=>{if(e.target===$(id))$(id).close();});
    }
    $('menuToggle').addEventListener('click',()=>{
      const expanded=$('menuToggle').getAttribute('aria-expanded')==='true';
      $('menuToggle').setAttribute('aria-expanded',String(!expanded));
      $('mobileNav').hidden=expanded;
    });
    $('mobileNav').querySelectorAll('a').forEach(a=>a.addEventListener('click',()=>{
      $('mobileNav').hidden=true;$('menuToggle').setAttribute('aria-expanded','false');
    }));
    window.addEventListener('hashchange',handleHash);
    applyLanguage();handleHash();
  }
  init();
})();
