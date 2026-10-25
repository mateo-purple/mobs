#!/usr/bin/env python3
"""No-dependency static site QA: bilingual copy, local art, ENEND backlink."""
from pathlib import Path
from html.parser import HTMLParser
import subprocess,json,re
ROOT=Path(__file__).resolve().parent
DOCS=ROOT.parent
class Scan(HTMLParser):
    def __init__(self):
        super().__init__()
        self.keys=set();self.placeholders=set();self.ids=set();self.hrefs=[];self.images=[]
    def handle_starttag(self,name,attrs):
        a=dict(attrs)
        if "data-t" in a:self.keys.add(a["data-t"])
        if "data-placeholder" in a:self.placeholders.add(a["data-placeholder"])
        if "id" in a:self.ids.add(a["id"])
        if name=="a" and "href" in a:self.hrefs.append(a["href"])
        if name=="img" and "src" in a:self.images.append(a["src"])
    handle_startendtag=handle_starttag
def node(args):
    r=subprocess.run(["node",*args],capture_output=True,text=True,encoding="utf-8")
    if r.returncode:raise AssertionError(r.stderr)
    return r.stdout
for js in ["content.js","app.js",str(DOCS/"app.js")]:
    node(["--check",str(ROOT/js) if js!="app.js" or js=="app.js" else str(ROOT/js)])
# above checks site content and app. ENEND app separately below
node(["--check",str(DOCS/"app.js")])
code="const fs=require('fs'),vm=require('vm'),ctx={window:{}};vm.runInNewContext(fs.readFileSync(process.argv[1],'utf8'),ctx);console.log(JSON.stringify(ctx.window.BS_CONTENT));"
content=json.loads(node(["-e",code,str(ROOT/"content.js")]))
texts=content["t"]
assert set(texts["ja"])==set(texts["en"]),"Translations missing entries"
assert len(content["terrain"])==6 and sum(x["count"] for x in content["terrain"])==33
assert len(content["mobs"])==8
assert len(content["relics"])==6
assert len(content["artifacts"])==6
scan=Scan();scan.feed((ROOT/"index.html").read_text(encoding="utf-8"))
assert scan.keys <= set(texts["ja"]),f"Missing page translation keys: {scan.keys-set(texts['ja'])}"
assert scan.placeholders <= set(texts["ja"]),f"Missing placeholders: {scan.placeholders-set(texts['ja'])}"
for href in scan.hrefs:
    if href.startswith("#"):assert href[1:] in scan.ids,href
    elif not href.startswith(("https://","http://","mailto:")):
        resolved=(ROOT/href).resolve()
        assert resolved.exists(),f"Broken internal href: {href}"
for image in scan.images:
    assert (ROOT/image).resolve().is_file(),f"Broken static image {image}"
for entry in content["mobs"]:
    if entry["art"]:assert (ROOT/entry["art"]).resolve().is_file(),f"Missing mob render {entry['art']}"
for entry in content["relics"]:
    assert (ROOT/"assets"/(entry["image"]+".png")).is_file()
for entry in content["artifacts"]:
    if entry["image"]:assert (ROOT/"assets"/(entry["image"]+".png")).is_file()
homepage=(DOCS/"index.html").read_text(encoding="utf-8")
for required in ["bedrock-strata/","strataHeading","strataLink","strataNav"]:
    assert required in homepage, f"ENEND not linked: {required}"
js=(DOCS/"app.js").read_text(encoding="utf-8")
for required in ["strataHeading","strataBody","strataLink"]:
    assert js.count(required)>=2,"Not bilingual on the ENEND side: "+required
assert "enend.lang" in (ROOT/"app.js").read_text(encoding="utf-8")
assert "作業台" in texts["ja"]["repairBody"] and "crafting grid" in texts["en"]["repairBody"].lower()
assert "anvil" in texts["ja"]["repairBody"].lower() or "金床" in texts["ja"]["repairBody"]
assert "not an anvil recipe" in texts["en"]["repairBody"].lower()
print("BEDROCK_SITE_QA_PASS",len(scan.keys),"UI translations, 33 terrains, 8 mobs, 6 relics, 6 artifacts, all images and ENEND crosslinks")
