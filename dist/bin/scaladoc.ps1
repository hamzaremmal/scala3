# Environment setup
$global:_EXITCODE = 0

$scriptPath = $PSScriptRoot
$global:_PROG_HOME = $scriptPath.TrimEnd('\')

. "$_PROG_HOME\bin\common.bat"
if ($LASTEXITCODE -ne 0) { goto end }

$global:_DEFAULT_JAVA_OPTS = "-Xmx768m -Xms768m"

args $args

# Main
classpathArgs

if ($env:JAVA_OPTS) {
    $global:_JAVA_OPTS = $env:JAVA_OPTS
} else {
    $global:_JAVA_OPTS = $global:_DEFAULT_JAVA_OPTS
}

$global:_JAVACMD = $env:_JAVACMD -replace '%', '%%'

& $_JAVACMD $global:_JAVA_OPTS $env:_JAVA_DEBUG $env:_JAVA_ARGS `
    -classpath "$env:_CLASS_PATH" `
    -Dscala.usejavacp=true `
    dotty.tools.scaladoc.Main $env:_SCALA_ARGS $env:_RESIDUAL_ARGS

if ($LASTEXITCODE -ne 0) {
    Write-Error "Error: Scaladoc execution failed"
    $global:_EXITCODE = 1
    goto end
}
goto end

# Subroutines

function args {
    $global:_JAVA_DEBUG = ""
    $global:_HELP = $false
    $global:_VERBOSE = $false
    $global:_QUIET = $false
    $global:_COLORS = $false
    $global:_SCALA_ARGS = ""
    $global:_JAVA_ARGS = ""
    $global:_RESIDUAL_ARGS = ""
    $global:_IN_SCRIPTING_ARGS = $false

    while ($args.Count -gt 0) {
        $arg = $args[0]
        switch ($arg) {
            "--" {
                $global:_IN_SCRIPTING_ARGS = $true
            }
            "-h" {
                $global:_HELP = $true
                addScala "-help"
            }
            "-help" {
                $global:_HELP = $true
                addScala "-help"
            }
            "-v" {
                $global:_VERBOSE = $true
                addScala "-verbose"
            }
            "-verbose" {
                $global:_VERBOSE = $true
                addScala "-verbose"
            }
            "-debug" {
                $global:_JAVA_DEBUG = $global:_DEBUG_STR
            }
            "-q" {
                $global:_QUIET = $true
            }
            "-quiet" {
                $global:_QUIET = $true
            }
            "-colors" {
                $global:_COLORS = $true
            }
            "-no-colors" {
                $global:_COLORS = $false
            }
            default {
                if ($arg.StartsWith("-D")) {
                    addJava "$arg"
                } elseif ($arg.StartsWith("-J")) {
                    addJava "${arg:2}"
                } else {
                    if ($global:_IN_SCRIPTING_ARGS) {
                        addScripting "$arg"
                    } else {
                        addResidual "$arg"
                    }
                }
            }
        }
        $args = $args[1..$args.Length]
    }
}

function addScala {
    param ($arg)
    $global:_SCALA_ARGS += " $arg"
}

function addJava {
    param ($arg)
    $global:_JAVA_ARGS += " $arg"
}

function addResidual {
    param ($arg)
    $global:_RESIDUAL_ARGS += " $arg"
}

function classpathArgs {
    $global:_ETC_DIR = "$global:_PROG_HOME\etc"
    loadClasspathFromFile
}

function loadClasspathFromFile {
    $global:_CLASS_PATH = ""
    $classpathFile = "$global:_ETC_DIR\scaladoc.classpath"
    if (Test-Path $classpathFile) {
        $lines = Get-Content $classpathFile
        foreach ($line in $lines) {
            $lib = "$global:_PROG_HOME\maven2\$line".Replace('/', '\')
            if (!$global:_CLASS_PATH) {
                $global:_CLASS_PATH = $lib
            } else {
                $global:_CLASS_PATH += "$env:_PSEP$lib"
            }
        }
    }
}

# Cleanups
end
exit $global:_EXITCODE
