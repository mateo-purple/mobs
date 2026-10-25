(() => {
  'use strict';
  const books=window.BS_CONTENT;
  if(!books)return;
  const $=(id)=>document.getElementById(id);
  const storage={
    get(k){try{return localStorage.getItem(k)}catch{return null}},
    set(k,v){try{localStorage.setItem(k,v)}catch{}}
  };
  const param=new URLSearchParams(location.search).get('lang');
  let lang=param==='ja'||param==='en'?param:(storage.get('enend.lang')==='en'?'en':'ja');
  function paragraphs(element,lines){
    const frag=document.createDocumentFragment();
    for(const s of lines){
      const p=document.createElement('p');
      p.textContent=s;
      frag.append(p);
    }
    element.replaceChildren(frag);
  }
  function definition(element,items){
    const frag=document.createDocumentFragment();
    for(const item of items){
      const group=document.createElement('div');group.className='entry';
      const dt=document.createElement('dt');dt.textContent=item.name;
      const dd=document.createElement('dd');dd.textContent=item.effect;
      group.append(dt,dd);frag.append(group);
    }
    element.replaceChildren(frag);
  }
  function render(){
    const t=books[lang];
    document.documentElement.lang=lang;
    document.title='BEDROCK STRATA | '+t.subtitle;
    $('skipLink').textContent=lang==='ja'?'本文へ':'Skip to content';
    $('subtitle').textContent=t.subtitle;
    paragraphs($('prefaceText'),t.preface);
    paragraphs($('landscapeText'),t.landscape);
    paragraphs($('creaturesText'),t.creatures);
    for(const [id,key] of [
      ['relicsTitle','relicsTitle'],['dragonTitle','dragonTitle'],
      ['nightmareTitle','nightmareTitle'],['repairTitle','repairTitle'],
      ['relicsIntro','relicsIntro'],['dragonIntro','dragonIntro'],
      ['nightmareIntro','nightmareIntro'],['otherText','other']
    ])$(id).textContent=t[key];
    definition($('relicList'),t.relics);
    definition($('dragonList'),t.dragon);
    definition($('nightmareList'),t.nightmare);
    definition($('repairList'),[t.repair]);
    for(const [id,txt] of Object.entries(lang==='ja'?{
      navGuide:'本文',navRelics:'発掘素材',navDragon:'竜鱗',navNightmare:'被膜',navRepair:'修繕',
      enendFooter:'ENEND Mob図鑑へ ↗',backTop:'先頭に戻る ↑'
    }:{
      navGuide:'Guide',navRelics:'Relics',navDragon:'Dragon Scales',
      navNightmare:'Nightmare Membrane',navRepair:'Restoration',
      enendFooter:'ENEND Mobdex ↗',backTop:'Back to top ↑'
    }))$(id).textContent=txt;
    for(const btn of document.querySelectorAll('[data-lang]')){
      const selected=btn.dataset.lang===lang;
      btn.classList.toggle('active',selected);
      btn.setAttribute('aria-pressed',String(selected));
    }
  }
  document.querySelectorAll('[data-lang]').forEach(btn=>btn.addEventListener('click',()=>{
    if(btn.dataset.lang===lang)return;
    lang=btn.dataset.lang;storage.set('enend.lang',lang);render();
  }));
  render();
})();