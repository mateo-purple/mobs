from pathlib import Path
import json,subprocess
from html.parser import HTMLParser
ROOT=Path(__file__).resolve().parent;DOCS=ROOT.parent
def run(*cmd):
 p=subprocess.run(cmd,capture_output=True,text=True,encoding="utf-8")
 if p.returncode:raise AssertionError(p.stderr or p.stdout)
 return p.stdout
for f in (ROOT/"content.js",ROOT/"app.js",DOCS/"app.js"):
 run("node","--check",str(f))
node="const vm=require('vm'),fs=require('fs'),c={window:{}};vm.runInNewContext(fs.readFileSync(process.argv[1],'utf8'),c);console.log(JSON.stringify(c.window.BS_CONTENT));"
books=json.loads(run("node","-e",node,str(ROOT/"content.js")))
assert set(books)=={"ja","en"}
for lang in ("ja","en"):
 b=books[lang]
 for k,size in (("preface",3),("landscape",2),("creatures",2),("relics",5),("dragon",4),("nightmare",8)):
  assert len(b[k])==size,(lang,k)
 assert len(b["repair"]["effect"])>40
 assert len(b["dragonIntro"])>20 and len(b["nightmareIntro"])>20
 assert len(b["other"])>30
 assert b["relics"][0]["id"]=="ruby"
 assert "作業台" in b["repair"]["effect"] if lang=="ja" else "crafting table" in b["repair"]["effect"]
 assert ("1個消費" in b["relics"][0]["effect"]) if lang=="ja" else ("one ruby" in b["relics"][0]["effect"])
html=(ROOT/"index.html").read_text(encoding="utf-8")
assert "<img" not in html and "<svg" not in html,"Unexpected hero/media"
assert "chapter-num" not in html and "CHAPTER 01" not in html,"Unrequested chapter decorations"
for key in ("id=\"prefaceText\"","id=\"landscapeText\"","id=\"creaturesText\"",
            "id=\"relicList\"","id=\"dragonList\"","id=\"nightmareList\"","id=\"repairList\""):
 assert key in html,key
assert 'href="../"' in html and 'href="bedrock-strata/"' in (DOCS/"index.html").read_text(encoding="utf-8")
assert "enend.lang" in (ROOT/"app.js").read_text(encoding="utf-8")
print("LYDIA_COMPLETE_GUIDE_PASS 8 original prose paragraphs; 5+4+8 enchantments; ruby one use; one crafting repair; ENEND reciprocal links; no imagery")
