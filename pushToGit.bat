@ECHO OFF
git add .
git commit -m "lokesh %TIME% %DATE%"
git pull
git push
PAUSE