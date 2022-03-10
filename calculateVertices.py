lat = 31
lon = 29
width = 1
height = 1
res = 1
pixels = 3601

def main():
    tiles = width*height
    verticesPerTiles = int((pixels/res))**2
    vertices = tiles * verticesPerTiles

    print(f"tiles : {tiles}")
    print(f"vertices per tile : {verticesPerTiles}")
    print(f"vertices : {int(vertices)} - {len(str(int(vertices)))}")

if __name__=="__main__":
    main()