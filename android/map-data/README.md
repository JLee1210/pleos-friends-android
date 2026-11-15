# Offline geographic demonstration data

`metro.json.gz` and `busan.json.gz` contain derived OpenStreetMap road/area extracts. Each file records its bounding box, source, geometry kind and longitude/latitude points. The street and park features were selected through Overpass on 2026-11-15; they are not proprietary Hyundai maps or live vehicle/traffic data.

© OpenStreetMap contributors. These derived databases are distributed under the Open Database License 1.0: https://opendatacommons.org/licenses/odbl/1-0/ . Attribution and licensing information: https://www.openstreetmap.org/copyright . The rendered maps also display attribution inside the application and recordings.

Run `scripts/render_demo_maps.py` with Pillow to reproduce `app/src/main/assets/maps/*.webp`. The app loads those bundled renderings offline and draws the scenario's independent routes, car markers, ETA pills and navigation destination on top. Geographic queries and routing asset preparation are not runtime dependencies.
