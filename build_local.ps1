$ErrorActionPreference = "Stop"

$javaHome = "C:\Program Files\Microsoft\jdk-21.0.10.7-hotspot"

if (-not (Test-Path $javaHome)) {
    throw "Java 21 was not found at '$javaHome'. Update this script if your JDK is installed elsewhere."
}

$env:JAVA_HOME = $javaHome
$env:Path = "$($env:JAVA_HOME)\bin;$($env:Path)"
$env:GRADLE_USER_HOME = Join-Path $PSScriptRoot ".gradle-user"

if (-not (Test-Path $env:GRADLE_USER_HOME)) {
    New-Item -ItemType Directory -Path $env:GRADLE_USER_HOME | Out-Null
}

& "$PSScriptRoot\gradlew.bat" build
