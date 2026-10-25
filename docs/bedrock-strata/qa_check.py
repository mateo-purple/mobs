#!/usr/bin/env python3
"""Check that the public site contains Lydia's manuscript, not extra lore."""
from pathlib import Path
from html.parser import HTMLParser
import json,subprocess
ROOT=Path(__file__).resolve().parent
DOCS=ROOT.parent
class Scan(HTMLParser):
    def __init__(self):
        super().__init__();self.ids=set();self.links=[];self.images=[]
    def handle_starttag(self,tag,attrs):
        a=dict(attrs)
        if a.get("id"):self.ids.add(a["id"])
        if tag=="a" and a.get("href"):self.links.append(a["href"])
        if tag=="img" and a.get("src"):self.images.append(a["src"])
    handle_startendtag=handle_starttag
def node(args):
    p=subprocess.run(["node",*args],capture_output=True,text=True,encoding="utf-8")
    assert p.returncode==0,p.stderr
    return p.stdout
for p in [ROOT/"content.js",ROOT/"app.js",DOCS/"app.js"]:
    node(["--check",str(p)])
program="""const fs=require('fs'),vm=require('vm'),ctx={window:{}};vm.runInNewContext(fs.readFileSync(process.argv[1],'utf8'),ctx);console.log(JSON.stringify(ctx.window.BS_CONTENT));"""
books=json.loads(node(["-e",program,str(ROOT/"content.js")]))
assert set(books)=={"ja","en"}
for lang in ("ja","en"):
    t=books[lang]
    assert len(t["preface"])==3
    assert len(t["landscape"])==2
    assert len(t["creatures"])==2
    assert len(t["relics"])==6
    assert t["other"] and t["relicsIntro"]
    assert [x["id"] for x in t["relics"]]==["ruby","gear","rose","feather","reactor","glowing_obsidian"]
assert "作業台" in books["ja"]["relics"][-1]["effect"]
assert "crafting table" in books["en"]["relics"][-1]["effect"]
assert "金床" in books["ja"]["relicsIntro"] and "anvil" in books["en"]["relicsIntro"]
assert books["ja"]["creaturesTitle"].startswith("第2章")
assert books["en"]["creaturesTitle"].startswith("Chapter 2")
for language in ("ja","en"):
    for card in books[language]["relics"]:
        assert card["effect"].strip() and card["name"].strip()
        assert (ROOT/"assets"/(card["image"]+".png")).is_file()
html=(ROOT/"index.html").read_text(encoding="utf-8")
scan=Scan();scan.feed(html)
for fragment in scan.links:
    if fragment.startswith("#"):
        assert fragment[1:] in scan.ids,fragment
    elif not fragment.startswith(("https://","http://","mailto:")):
        assert (ROOT/fragment).resolve().exists(),"Missing internal link "+fragment
for image in scan.images:
    assert (ROOT/image).is_file(),"Missing illustration "+image
for banned in ("terrainGrid","terrainTabs","mobGrid","mobSearch","boss-grid",
               "artifactGrid","relic-count","bossStrategy","relicIntroGuide"):
    assert banned not in html,"Unrequested extra encyclopedia entry still shown: "+banned
assert "terrain:" not in (ROOT/"content.js").read_text(encoding="utf-8")
assert "mobs:" not in (ROOT/"content.js").read_text(encoding="utf-8")
assert "artifacts:" not in (ROOT/"content.js").read_text(encoding="utf-8")
assert "id=\"companion-guide\"" not in (DOCS/"index.html").read_text(encoding="utf-8")
assert 'href="bedrock-strata/"' in (DOCS/"index.html").read_text(encoding="utf-8")
assert 'href="../"' in html
assert "enend.lang" in (ROOT/"app.js").read_text(encoding="utf-8")
assert (ROOT/"manuscript.css").is_file()
print("LYDIA_MANUSCRIPT_ONLY_PASS bilingual 3+2+2 chapters, six original relic descriptions, ENEND links, no invented intros")
