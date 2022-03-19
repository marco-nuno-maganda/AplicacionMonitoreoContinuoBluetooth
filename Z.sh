NombreRepositorio=AplicacionMonitoreoContinuoBluetooth 
Rama=main2
rm -rf .git


git init
#git add PythonBasicos/*.*
git add *.*
git commit -m "Primer commit"
git remote add origin https://github.com/marco-nuno-maganda/${NombreRepositorio}.git
git branch -M ${Rama}
git push -u origin ${Rama}
