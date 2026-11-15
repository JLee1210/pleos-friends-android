#!/usr/bin/env python3
"""Render the bundled ODbL geographic extracts into offline grayscale demo maps.

Optional developer tool: python3 -m venv .map-venv && .map-venv/bin/pip install Pillow
Then: .map-venv/bin/python scripts/render_demo_maps.py
The app uses the checked-in WebP assets and needs no Python or map API key.
"""
import json,gzip,pathlib,math
from PIL import Image,ImageDraw,ImageFont
base=pathlib.Path(__file__).resolve().parents[1]
assets=base/'app/src/main/assets/maps';assets.mkdir(exist_ok=True)
source=base/'map-data';source.mkdir(exist_ok=True)
fontpath=str(base/'app/src/main/res/font/asta_review_semibold.ttf')
for name,bbox,labels in [
 ('metro',[126.35,37.02,127.55,37.88],[(126.768,37.68,'고양시 일산'),(126.86,37.49,'서울특별시'),(126.96,37.39,'안양시'),(127.03,37.30,'수원시'),(127.2,37.26,'용인시'),(127.06,37.61,'노원구'),(126.82,37.60,'한강'),(127.02,37.71,'북한산'),(127.21,37.52,'하남시')]),
 ('busan',[128.94,35.08,129.36,35.40],[(129.06,35.18,'부산광역시'),(129.16,35.19,'해운대구'),(129.22,35.28,'기장군'),(129.04,35.29,'금정구'),(129.28,35.13,'동해'),(128.99,35.13,'사하구')])]:
 with gzip.open(source/(name+'.json.gz'),'rt') as f:data=json.load(f)
 bbox=data['bbox'];features=data['features']
 W=2800;H=round(W*(bbox[3]-bbox[1])/((bbox[2]-bbox[0])*math.cos(math.radians((bbox[1]+bbox[3])/2))))
 im=Image.new('RGB',(W,H),'#dadbdd');draw=ImageDraw.Draw(im)
 def project(points):return [((x-bbox[0])/(bbox[2]-bbox[0])*W,(bbox[3]-y)/(bbox[3]-bbox[1])*H) for x,y in points]
 colors={'urban':'#d0d1d2','park':'#c9ccca','forest':'#c5c8c6','water':'#b3b9be'}
 for kind,pts in sorted(features,key=lambda x: ['urban','forest','park','water'].index(x[0]) if x[0] in colors else 5):
  if kind in colors and len(pts)>2:draw.polygon(project(pts),fill=colors[kind])
 roads=[(k,p) for k,p in features if k not in colors]
 order={'service':0,'residential':1,'unclassified':1,'tertiary':2,'secondary':3,'primary':4,'trunk':5,'motorway':6}
 for kind,pts in sorted(roads,key=lambda x:order.get(x[0].replace('_link',''),3)):
  points=project(pts);major=order.get(kind.replace('_link',''),3)
  w=[1,2,3,4,5,6,7][major]
  if major>=3:draw.line(points,fill='#a8adb1',width=w+3,joint='curve')
  draw.line(points,fill='#f7f8f8' if major<5 else '#eceeef',width=w,joint='curve')
 font=ImageFont.truetype(fontpath,39)
 for lon,lat,text in labels:
  x,y=project([(lon,lat)])[0];draw.text((x,y),text,font=font,anchor='mm',fill='#777d81',stroke_width=3,stroke_fill='#e5e6e7')
 im.save(assets/(name+'.webp'),quality=91,method=6)
 print(name,len(features),im.size,(assets/(name+'.webp')).stat().st_size,flush=True)
