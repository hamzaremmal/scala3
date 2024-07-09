# #########################################################################
# ## Code common to scalac.bat, scaladoc.bat and scala.bat

if ($env:JAVACMD) {
  $global:_JAVACMD = $env:JAVACMD
} elseif ($env:JAVA_HOME) {
  $global:_JAVACMD = Join-Path $env:JAVA_HOME "bin\java.exe"
} elseif ($env:JDK_HOME) {
  $global:_JAVACMD = Join-Path $env:JDK_HOME "bin\java.exe"
} else {
  $javaPath = Get-Command java.exe -ErrorAction SilentlyContinue
  if ($javaPath) {
      $javaPathDir = Split-Path $javaPath -Parent
      if ($javaPathDir -notmatch "javapath") {
          $global:_JAVACMD = Join-Path $javaPathDir "java.exe"
      }
  }

  if (-not $global:_JAVACMD) {
      $programFilesJava = Join-Path $env:ProgramFiles "Java"
      $javaHome = Get-ChildItem -Path $programFilesJava -Directory -Filter "jre*" | Select-Object -First 1
      if ($javaHome) {
          $global:_JAVA_HOME = $javaHome.FullName
      } else {
          $optPath = "C:\opt"
          $javaHome = Get-ChildItem -Path $optPath -Directory -Filter "jdk*" | Select-Object -First 1
          if ($javaHome) {
              $global:_JAVA_HOME = Join-Path $javaHome.FullName "jre"
          }
      }

      if ($global:_JAVA_HOME) {
          $global:_JAVACMD = Join-Path $global:_JAVA_HOME "bin\java.exe"
      }
  }
}

if (-not (Test-Path $global:_JAVACMD)) {
  Write-Error "Error: Java executable not found ($global:_JAVACMD)"
  $global:_EXITCODE = 1
  exit $global:_EXITCODE
}

if (-not $global:_PROG_HOME) {
  Write-Error "Error: Variable _PROG_HOME undefined"
  $global:_EXITCODE = 1
  exit $global:_EXITCODE
}

$global:_ETC_DIR = Join-Path $global:_PROG_HOME "etc"

$global:_PSEP = ";"
