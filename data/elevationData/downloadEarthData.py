from pathlib import Path
from requests import Session
import enlighten
import json
import os.path
import threading

import concurrent.futures

def setup() :
    with open("data/private/credentials.json", "r") as f:
        credentials = dict(json.loads(f.read()))

    urls = []
    with open("data/elevationData/downloadURLs.txt", "r") as f:
        for line in f.readlines():
            if "num" not in line:
                urls.append(line.strip())

    return urls, credentials

def download(url, credentials):
    file_name = Path(url).name
    # print(credentials.get("path") + file_name)

    if (not os.path.exists(credentials.get("path") + file_name)) :
        session = Session()
        session.auth = (credentials.get("username"), credentials.get("password"))
        _redirect = session.get(url)
        _response = session.get(_redirect.url)
        with open(credentials.get("path") + file_name, 'wb') as file:
            file.write(_response._content)
    return file_name # does not accepting making threads work (I think)

def main():
    urls, credentials = setup()
    ## testcase
    # urls = urls[0:2]

    print("URLs to download : " + str(len(urls)))
    bar = enlighten.Counter(total=len(urls), desc="images", unit="img")

    with concurrent.futures.ThreadPoolExecutor(max_workers=6) as downloadPool:
        threads = []
        for url in urls:
            threads.append(downloadPool.submit(download, url, credentials))
        for thread in concurrent.futures.as_completed(threads):
            bar.update()
            # print(thread.result())

if __name__=="__main__":
    main()