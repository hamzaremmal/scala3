###################################################################################################
###                                  POWERSHELL COMMON SCRIPT                                   ###
###                                                                                             ###
### Author: Hamza REMMAL <hamza.remmal@epfl.ch>                                                 ###
### Since : Scala 3.5.0                                                                         ###
###################################################################################################


###################################################################################################
######################################## UTILITY FUNCTIONS ########################################
###################################################################################################

function Scala-GetJavaClasspathSeparator {
  if ($IsWindows) {
      return ';'
  } else {
      return ':'
  }
}

function Scala-FetchScalaVersion {
  if (Test-Path $_VERSION_FILE) {
      foreach ($line in Get-Content $_VERSION_FILE) {
          if ($line.StartsWith("version:=")) {
              return $line.Substring(9)
          }
      }
      Write-Error "Error: 'VERSION' file does not contain 'version' entry in ($_VERSION_FILE)"
  } else {
      Write-Error "Error: 'VERSION' file is missing in ($_VERSION_FILE)"
  }
}

function Scala-LoadClasspathFromFile {
  param ( [string] $file )
  $_CLASS_PATH_RESULT = ""
  if (Test-Path $file) {
    foreach ($line in Get-Content $file) {
      $lib = "$_PROG_HOME/maven2/$line"
      if (!$_CLASS_PATH_RESULT) {
        $_CLASS_PATH_RESULT = $lib
      } else {
        $_CLASS_PATH_RESULT += "$_PSEP$lib"
      }
    }
  }

  return $_CLASS_PATH_RESULT
}


###################################################################################################
############################################ LOAD JAVA ############################################
###################################################################################################

$javaPath = Get-Command java -ErrorAction SilentlyContinue
if ($javaPath) { $_JAVACMD = $javaPath.Source }

if (-not (Test-Path $_JAVACMD)) {
  Write-Error "Error: Java executable not found ($_JAVACMD)"
  exit 1
}

if (-not $_PROG_HOME) {
  Write-Error "Error: Variable _PROG_HOME undefined"
  exit 1
}

###################################################################################################
######################################## VARIOUS VARIABLES ########################################
###################################################################################################

$_ETC_DIR        = Join-Path $_PROG_HOME "etc"
$_BIN_DIR        = Join-Path $_PROG_HOME "bin"
$_MVN_REPOSITORY = "file:///$($_PROG_HOME -replace '\\', '/')/maven2"
$_VERSION_FILE   = Join-Path $_PROG_HOME "VERSION"
$_PSEP           = Scala-GetJavaClasspathSeparator
