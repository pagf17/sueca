@echo off
REM ─── Sueca - Build Script (Windows) ───
echo Compilando Sueca...

REM Caminho do Launch4j
set "LAUNCH4J=C:\Program Files (x86)\Launch4j\launch4jc.exe"

REM Tentar encontrar o JDK automaticamente
set "JAVA_HOME_FOUND="

REM Verificar se javac ja esta no PATH
where javac >nul 2>&1
if %ERRORLEVEL% equ 0 (
    set "JAVAC=javac"
    set "JAR=jar"
    goto :compile
)

REM Procurar em locais comuns
for /d %%D in ("C:\Program Files\Java\jdk*") do set "JAVA_HOME_FOUND=%%D"
for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk*") do set "JAVA_HOME_FOUND=%%D"
for /d %%D in ("C:\Program Files\Microsoft\jdk*") do set "JAVA_HOME_FOUND=%%D"
for /d %%D in ("C:\Program Files\Amazon Corretto\jdk*") do set "JAVA_HOME_FOUND=%%D"
for /d %%D in ("C:\Program Files\Zulu\zulu*") do set "JAVA_HOME_FOUND=%%D"

if defined JAVA_HOME_FOUND (
    echo JDK encontrado em: %JAVA_HOME_FOUND%
    set "JAVAC=%JAVA_HOME_FOUND%\bin\javac"
    set "JAR=%JAVA_HOME_FOUND%\bin\jar"
    goto :compile
)

echo.
echo ERRO: JDK nao encontrado!
echo.
echo Instala o JDK a partir de: https://adoptium.net
echo Depois volta a executar este script.
echo.
pause
exit /b 1

:compile
REM Limpar build anterior
if exist build rmdir /s /q build
if exist dist rmdir /s /q dist
mkdir build
mkdir dist

REM Compilar
dir /s /b src\*.java > sources.txt
"%JAVAC%" --release 21 -d build @sources.txt

if %ERRORLEVEL% neq 0 (
    echo.
    echo ERRO na compilacao!
    pause
    exit /b 1
)

REM Copiar recursos
if exist src\sueca\resources (
    xcopy /s /e /y src\sueca\resources build\sueca\resources\ >nul
)

REM Criar manifest
echo Main-Class: sueca.SuecaGame> build\MANIFEST.MF

REM Criar JAR
cd build
"%JAR%" cfm ..\dist\Sueca.jar MANIFEST.MF sueca\
cd ..

if exist dist\Sueca.jar (
    del sources.txt 2>nul

    REM Criar EXE com Launch4j
    echo Criando Sueca.exe com icone...
    "%LAUNCH4J%" launch4j-config.xml

    if exist dist\Sueca.exe (
        echo.
        echo ===================================
        echo   Build completo com sucesso!
        echo   Ficheiro: dist\Sueca.exe
        echo ===================================
        echo.
        echo Para jogar, faz duplo clique em dist\Sueca.exe
        echo.
    ) else (
        echo.
        echo AVISO: EXE nao criado, mas JAR esta disponivel.
        echo Para jogar: java -jar dist\Sueca.jar
        echo.
    )

    REM Perguntar se quer jogar ja
    set /p PLAY="Queres jogar agora? (S/N): "
    if /i "%PLAY%"=="S" (
        if exist dist\Sueca.exe (
            start "" dist\Sueca.exe
        ) else (
            start "" java -jar dist\Sueca.jar
        )
    )
) else (
    echo.
    echo ERRO ao criar o JAR!
    pause
)
