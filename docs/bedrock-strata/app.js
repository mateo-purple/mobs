/* Original Lydia manuscript only. */
(() => {
 'use strict';
 const books=window.BS_CONTENT;
 if (!books) return;
 const $ = id => document.getElementById(id);
 const keys = {
   navPreface: 'navPreface', navLandscape: 'navLandscape',
   navCreatures: 'navCreatures', navRelics: 'navRelics',
   subtitle: 'subtitle', heroEyebrow: 'heroEyebrow', jumpText: 'jump',
   prefaceTitle: 'prefaceTitle', landscapeTitle: 'landscapeTitle',
   creaturesTitle: 'creaturesTitle', relicsTitle: 'relicsTitle',
   relicsIntro: 'relicsIntro', otherText: 'other'
 };
 const storage = {
   get(key) { try { return localStorage.getItem(key); } catch { return null; } },
   set(key,val) { try { localStorage.setItem(key,val); } catch {} }
 };
 const param = new URLSearchParams(location.search).get('lang');
 let language = (param === 'ja' || param === 'en') ? param :
   storage.get('enend.lang') === 'en' ? 'en' : 'ja';
 function paragraphs(container, copy) {
   const node = document.createDocumentFragment();
   for(const part of copy) {
     const p = document.createElement('p');
     p.textContent = part;
     node.appendChild(p);
   }
   container.replaceChildren(node);
 }
 function buildRelics(items) {
   const fragment=document.createDocumentFragment();
   for(let i=0;i<items.length;i++) {
     const item=items[i];
     const card=document.createElement('article');
     card.className='relic-card relic-from-manuscript';
     const number=document.createElement('span');
     number.className='relic-no';
     number.textContent='#'+String(i+1).padStart(2,'0');
     const picture=document.createElement('div');
     picture.className='relic-graphic';
     const img=document.createElement('img');
     img.src='assets/'+item.image+'.png';
     img.alt=item.name;
     img.width=59; img.height=59; img.loading='lazy';
     picture.appendChild(img);
     const heading=document.createElement('h3');
     heading.textContent=item.name;
     const ability=document.createElement('div');
     ability.className='relic-ability';
     ability.textContent=item.label;
     const description=document.createElement('p');
     description.className='relic-effect';
     description.textContent=item.effect;
     card.append(number,picture,heading,ability,description);
     fragment.appendChild(card);
   }
   $('relicGrid').replaceChildren(fragment);
 }
 function render() {
   const book=books[language];
   document.documentElement.lang=language;
   document.title='BEDROCK STRATA | '+book.subtitle;
   $('skipLink').textContent=book.jump;
   for(const [element,key] of Object.entries(keys)) $(element).textContent=book[key];
   $('heroLine').textContent=language==='ja'?'答えは無ではない。':'The answer is not nothingness.';
   paragraphs($('prefaceText'),book.preface);
   paragraphs($('landscapeText'),book.landscape);
   paragraphs($('creaturesText'),book.creatures);
   buildRelics(book.relics);
   $('enendFooter').textContent=book.footerEnend+' ↗';
   $('backTop').textContent=book.footerTop+' ↑';
   for(const button of document.querySelectorAll('[data-lang]')) {
     const selected=button.dataset.lang===language;
     button.classList.toggle('is-active',selected);
     button.setAttribute('aria-pressed',String(selected));
   }
 }
 for(const button of document.querySelectorAll('[data-lang]')) {
   button.addEventListener('click',()=>{
     if(button.dataset.lang===language)return;
     language=button.dataset.lang;
     storage.set('enend.lang',language);
     render();
   });
 }
 const menu=$('menuButton');
 menu.addEventListener('click',()=>{
   const opened=$('mainNav').classList.toggle('is-open');
   menu.setAttribute('aria-expanded',String(opened));
 });
 $('mainNav').querySelectorAll('a').forEach(anchor=>anchor.addEventListener('click',()=>{
   $('mainNav').classList.remove('is-open');
   menu.setAttribute('aria-expanded','false');
 }));
 render();
})();
