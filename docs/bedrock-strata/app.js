(() => {
  'use strict';
  const content = window.BS_CONTENT;
  if (!content) return;
  const $ = (id) => document.getElementById(id);
  const esc = (s) => String(s).replace(/[&<>"']/g, (c) => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
  const safeStorage = {
    get(k) { try { return localStorage.getItem(k); } catch { return null; } },
    set(k,v) { try { localStorage.setItem(k,v); } catch {} }
  };
  const urlLang = new URLSearchParams(window.location.search).get('lang');
  let lang = urlLang === 'en' || urlLang === 'ja' ? urlLang : safeStorage.get('enend.lang') === 'en' ? 'en' : 'ja';
  let currentTerrain = 'all';
  let currentMob = 'all';
  let query = '';

  const text = (key) => content.t[lang][key] ?? key;
  const name = (item) => item.name[lang];
  const img = (id) => 'assets/' + encodeURIComponent(id) + '.png';

  function renderTerrainTabs() {
    const tabs = [{id:'all',title:lang==='ja'?'すべて / 33':'All / 33'},...content.terrain.map(group=>({id:group.id,title:text(group.key)}))];
    $('terrainTabs').innerHTML = tabs.map(tab=>`<button type="button" role="tab" data-terrain="${esc(tab.id)}" aria-selected="${String(currentTerrain===tab.id)}" class="${currentTerrain===tab.id?'is-current':''}">${esc(tab.title)}</button>`).join('');
    $('terrainTabs').querySelectorAll('[data-terrain]').forEach(button=>button.addEventListener('click',()=>{
      currentTerrain=button.dataset.terrain;renderTerrainTabs();renderTerrain();
    }));
  }

  function renderTerrain() {
    const groups=content.terrain.filter(g=>currentTerrain==='all'||g.id===currentTerrain);
    $('terrainGrid').innerHTML=groups.map(g=>{
      const title=text(g.key),desc=text(g.key+'Desc'),regions=lang==='ja'?g.regionsJa:g.regionsEn;
      return `<article class="terrain-card">
        <div class="terrain-card-header"><div class="terrain-card-symbol" aria-hidden="true">${esc(g.icon)}</div><span>${g.count} / 33 · STRATA</span></div>
        <h3>${esc(title)}</h3><p>${esc(desc)}</p><div class="region-list">${esc(regions)}</div>
      </article>`;
    }).join('');
  }

  function renderMobFilters() {
    const filters=['all','legacy','danger','other'];
    $('mobFilters').innerHTML=filters.map(id=>{
      const key=id==='all'?'filterAll':id==='legacy'?'filterLegacy':id==='danger'?'filterDanger':'filterOther';
      return `<button type="button" class="${currentMob===id?'is-active':''}" data-mobtype="${id}" aria-pressed="${String(currentMob===id)}">${esc(text(key))}</button>`;
    }).join('');
    $('mobFilters').querySelectorAll('[data-mobtype]').forEach(btn=>btn.addEventListener('click',()=>{
      currentMob=btn.dataset.mobtype;renderMobFilters();renderMobs();
    }));
  }

  function renderMobs(){
    const needle=query.trim().toLocaleLowerCase();
    const matches=content.mobs.filter(m=>{
      if(currentMob!=='all'&&m.type!==currentMob)return false;
      if(!needle)return true;
      return [m.name.ja,m.name.en,m.summary.ja,m.summary.en,m.tip.ja,m.tip.en].some(s=>s.toLocaleLowerCase().includes(needle));
    });
    $('mobGrid').innerHTML=matches.map((m,index)=>{
      const thumbnail=m.art?`<img src="${esc(m.art)}" loading="lazy" alt="${esc(m.name[lang])}" />`:`<span class="mob-fallback" aria-hidden="true">${esc(m.mark)}</span>`;
      const category=m.type==='legacy'?'mobLegacyTag':m.type==='danger'?'mobDangerTag':'mobOtherTag';
      return `<article class="mob-card" id="mob-${esc(m.id)}">
        <div class="mob-image ${m.id==='undergroundPhantom'?'mob-phantom':''}">${thumbnail}</div>
        <div class="mob-card-content">
          <div class="mob-overline"><span>${esc(text(category))}</span><span>${String(index+1).padStart(2,'0')}</span></div>
          <h3>${esc(name(m))}</h3><div class="mob-alias">${esc(m.name[lang==='ja'?'en':'ja'])}</div>
          <p>${esc(m.summary[lang])}</p>
          <div class="mob-tip">${esc(m.tip[lang])}</div>
        </div>
      </article>`;
    }).join('');
    $('mobEmpty').hidden=matches.length>0;
  }

  function renderRelics(){
    $('relicGrid').innerHTML=content.relics.map((r,index)=>{
      const method=r.method==='craft'?'CRAFT':'ANVIL';
      return `<article class="relic-card ${r.method==='craft'?'is-repair':''}" id="relic-${esc(r.id)}">
        <span class="relic-no">#${String(index+1).padStart(2,'0')}</span>
        <div class="relic-graphic"><img src="${img(r.image)}" alt="${esc(name(r))}" loading="lazy" width="59" height="59"></div>
        <h3>${esc(name(r))}</h3><div class="relic-ability">${esc(r.ability[lang])}</div>
        <p>${esc(r.desc[lang])}</p>
        <div class="relic-foot"><span>${esc(r.slot[lang])}</span><span class="relic-method">${method}</span></div>
      </article>`;
    }).join('');
  }

  function renderArtifacts(){
    $('artifactGrid').innerHTML=content.artifacts.map(a=>{
      const icon=a.image?`<img src="${img(a.image)}" width="54" height="54" alt="${esc(name(a))}" loading="lazy">`:`<span aria-hidden="true">${esc(a.mark||'◇')}</span>`;
      return `<article class="artifact-card"><div class="artifact-icon">${icon}</div><div><h3>${esc(name(a))}</h3><p>${esc(a.desc[lang])}</p></div></article>`;
    }).join('');
  }

  const headings = new Set(['terrainHeading','bestiaryHeading','relicHeading','objectsHeading','finalHeading']);
  function renderText() {
    const dict=content.t[lang];
    document.documentElement.lang=lang;
    document.title=lang==='ja'?'BEDROCK STRATA | 深淵の博物誌':'BEDROCK STRATA | Compendium of the Abyss';
    document.querySelector('meta[name="description"]').setAttribute('content', lang==='ja' ?
      'BEDROCK STRATA 深淵の博物誌。33種類の地形、歴史的Mob、二大ボスと発掘素材を収録した日本語・英語対応攻略ガイド。' :
      'BEDROCK STRATA: Compendium of the Abyss. Explore 33 underground biomes, forgotten mobs, two bosses and lost relics in English or Japanese.');
    document.querySelectorAll('[data-t]').forEach(el=>{
      const key=el.dataset.t;
      if(!(key in dict))return;
      if(headings.has(key))el.innerHTML=dict[key]; else el.textContent=dict[key];
    });
    document.querySelectorAll('[data-placeholder]').forEach(el=>{
      const k=el.dataset.placeholder;if(k in dict)el.setAttribute('placeholder',dict[k]);
    });
    document.querySelectorAll('[data-lang]').forEach(btn=>{
      const selected=btn.dataset.lang===lang;
      btn.classList.toggle('is-active',selected);
      btn.setAttribute('aria-pressed',String(selected));
    });
    renderTerrainTabs();renderTerrain();renderMobFilters();renderMobs();renderRelics();renderArtifacts();
  }

  document.querySelectorAll('[data-lang]').forEach(button=>button.addEventListener('click',()=>{
    lang=button.dataset.lang;
    safeStorage.set('enend.lang',lang);
    renderText();
  }));
  $('mobSearch').addEventListener('input',ev=>{
    query=ev.target.value;renderMobs();
  });
  const menuButton=$('menuButton');
  menuButton.addEventListener('click',()=>{
    const open=$('mainNav').classList.toggle('is-open');
    menuButton.setAttribute('aria-expanded',String(open));
  });
  $('mainNav').querySelectorAll('a').forEach(a=>a.addEventListener('click',()=>{
    $('mainNav').classList.remove('is-open');
    menuButton.setAttribute('aria-expanded','false');
  }));
  if('IntersectionObserver' in window) {
    const links=Array.from(document.querySelectorAll('.main-nav a'));
    const observer=new IntersectionObserver(entries=>{
      for(const e of entries)if(e.isIntersecting){
        links.forEach(link=>link.classList.toggle('active',link.getAttribute('href')==='#'+e.target.id));
      }
    },{rootMargin:'-18% 0px -65% 0px',threshold:0});
    for(const section of document.querySelectorAll('main section[id]'))observer.observe(section);
  }
  renderText();
})();
