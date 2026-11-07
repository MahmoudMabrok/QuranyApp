from PIL import Image,ImageDraw,ImageFont,ImageFilter
from pathlib import Path
import json,math
P=Path(__file__).resolve().parent;ROOT=P.parent;FONT=str(ROOT/'composition/fonts/Cairo.ttf')
G='#075846';M='#F2F7EE';GOLD='#DAB65A'
def font(size,bold=False):
 f=ImageFont.truetype(FONT,size)
 if bold:
  try:f.set_variation_by_name('ExtraBold')
  except:pass
 return f
def text(im,s,y,size,color=G,bold=False,x=540):
 d=ImageDraw.Draw(im); f=font(size,bold)
 while d.textbbox((0,0),s,font=f,direction='rtl')[2]>im.width-160 and size>18:
  size-=1;f=font(size,bold)
 d.text((x,y),s,font=f,anchor='mm',direction='rtl',fill=color)
def star(d,cx,cy,r,color,width=2):
 pts=[]
 for i in range(16):
  a=math.pi/8*i-math.pi/2;rr=r if i%2==0 else r*.72;pts.append((cx+rr*math.cos(a),cy+rr*math.sin(a)))
 d.line(pts+[pts[0]],fill=color,width=width)
def device(im,src,x,y,w):
 shot=Image.open(src).convert('RGB');h=round(w*shot.height/shot.width)
 shadow=Image.new('RGBA',im.size,(0,0,0,0));sd=ImageDraw.Draw(shadow);sd.rounded_rectangle((x-12,y+10,x+w+12,y+h+28),radius=56,fill=(0,50,35,55));shadow=shadow.filter(ImageFilter.GaussianBlur(20));im.paste(shadow,(0,0),shadow)
 d=ImageDraw.Draw(im);d.rounded_rectangle((x-14,y-14,x+w+14,y+h+14),radius=55,fill='#163E33')
 shot=shot.resize((w,h),Image.Resampling.LANCZOS);mask=Image.new('L',(w,h));ImageDraw.Draw(mask).rounded_rectangle((0,0,w,h),radius=42,fill=255);im.paste(shot,(x,y),mask)
items=json.loads((P/'selected_screens.json').read_text())
for item in items:item['path']=str(ROOT/'real_screens'/(item['id']+'.png'))
for i,item in enumerate(items):
 im=Image.new('RGB',(1080,1920),M);d=ImageDraw.Draw(im)
 star(d,-70,175,460,'#DCE9DB',3);star(d,1035,1660,460,'#DCE9DB',3)
 d.text((90,100),'QURANY',font=font(26,True),fill=G,spacing=3)
 d.rounded_rectangle((934,83,992,141),radius=16,fill=G);d.text((963,110),str(i+1).zfill(2),font=font(24,True),fill='white',anchor='mm')
 text(im,item['headline'],234,90,bold=True);text(im,item['subheadline'],353,38,color='#4E7668')
 device(im,item['path'],160,480,760)
 f=P/f"{i+1:02d}_{item['id']}.png";im.convert('RGB').save(f,optimize=True);item['output']=str(f)
# Feature graphic: real UI plus a restrained promise, no store badges or rank claims.
im=Image.new('RGB',(1024,500),G);d=ImageDraw.Draw(im);star(d,0,500,400,'#216F58',2);star(d,1000,-30,350,'#216F58',2)
shot=Image.open(items[0]['path']).convert('RGB');shot.thumbnail((235,405),Image.Resampling.LANCZOS);im.paste(shot,(115+(235-shot.width)//2,48))
text(im,'قرآني',171,112,color='white',bold=True,x=690);text(im,'رحلتك مع القرآن',283,49,color='#EAF4E6',x=690);text(im,'قراءة • تفسير • استماع • حفظ',370,27,color='#DCC477',x=690)
im.save(P/'feature_graphic_1024x500.png',optimize=True)
# Compact contact sheet only; the source screenshots remain full resolution.
sheet=Image.new('RGB',(1080,1050),'#E4EEE1')
for i,item in enumerate(items):
 im=Image.open(item['output']).resize((270,480),Image.Resampling.LANCZOS);sheet.paste(im,((i%4)*270,(i//4)*525));ImageDraw.Draw(sheet).text(((i%4)*270+12,(i//4)*525+485),Path(item['output']).name,font=ImageFont.load_default(),fill=G)
sheet.save(P/'contact_sheet.png')
report=[]
for f in sorted(P.glob('*.png')):
 if f.name=='contact_sheet.png' or f.name=='launcher_source_192.png':continue
 im=Image.open(f); report.append({'file':f.name,'size':list(im.size),'mode':im.mode,'has_alpha':im.mode=='RGBA','bytes':f.stat().st_size})
for r in report:
 if r['file']=='app_icon_512.png':assert r['size']==[512,512] and r['mode']=='RGBA' and r['bytes']<1024*1024
 elif r['file'].startswith('feature'):assert r['size']==[1024,500] and r['mode']=='RGB'
 else:assert r['size']==[1080,1920] and r['mode']=='RGB'
(P/'validation.json').write_text(json.dumps(report,ensure_ascii=False,indent=2));(P/'selected_screens.json').write_text(json.dumps(items,ensure_ascii=False,indent=2));print(json.dumps(report,indent=2))
