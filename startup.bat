@echo off

mkdir "data\\private"
mkdir "data\\STLs"
mkdir "data\\OBJs"

if not exist data/private/credentials.json (
    set /p username="Earthdata username : "
    set /p password="Password : "
    set /p path="Path for files (+-350gb) : "
    echo {"username":"%username%","password":"%password%","path":"%path%"} > data/private/credentials.json
    echo credentials set
) else (
    echo credentials already set
)
