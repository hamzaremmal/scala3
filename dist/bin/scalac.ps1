# Environment setup
$global:_EXITCODE = 0

$scriptPath = $PSScriptRoot
$global:_PROG_HOME = $scriptPath.TrimEnd('\')

. "$_PROG_HOME\bin\common.ps1"
if ($LASTEXITCODE -ne 0) { goto end }

args $args

# Main
compilerJavaClasspathArgs

$global:_JAVACMD = $env:_JAVACMD -replace '%', '%%'

& $_JAVACMD $env:_JAVA_ARGS -classpath "$env:_JVM_CP_ARGS" "-Dscala.usejavacp=true" "-Dscala.home=$_PROG_HOME" dotty.tools.MainGenericCompiler $env:_SCALA_ARGS
if ($LASTEXITCODE -ne 0) {
    $global:_EXITCODE = 1
    goto end
}
goto end

# Subroutines

function args {
    $global:_JAVA_ARGS = ""
    $global:_SCALA_ARGS = ""
    $global:_SCALA_CPATH = ""
    $global:_CONSUME_REMAINING = $false

    while ($args.Count -gt 0) {
        $arg = $args[0]
        if ($global:_CONSUME_REMAINING) {
            $global:_SCALA_ARGS += " `"$arg`""
            $args = $args[1..$args.Length]
        } elseif ($arg -eq "--" -or $arg -eq "-script") {
            $global:_CONSUME_REMAINING = $true
            $global:_SCALA_ARGS += " `"$arg`""
            $args = $args[1..$args.Length]
        } elseif ($arg -eq "-Oshort") {
            $global:_JAVA_ARGS += " `"-XX:+TieredCompilation`" `"-XX:TieredStopAtLevel=1`""
            $global:_SCALA_ARGS += " -Oshort"
            $args = $args[1..$args.Length]
        } elseif ($arg.StartsWith("-D")) {
            $global:_JAVA_ARGS += " `"$arg`""
            $global:_SCALA_ARGS += " `"$arg`""
            $args = $args[1..$args.Length]
        } elseif ($arg.StartsWith("-J")) {
            $global:_JAVA_ARGS += " `"${arg:2}`""
            $global:_SCALA_ARGS += " `"$arg`""
            $args = $args[1..$args.Length]
        } elseif ($arg -eq "-classpath" -or $arg -eq "-cp") {
            $global:_SCALA_CPATH = $args[1]
            $args = $args[2..$args.Length]
        } else {
            $global:_SCALA_ARGS += " `"$arg`""
            $args = $args[1..$args.Length]
        }
    }
}

function compilerJavaClasspathArgs {
    $CP_FILE = "$env:_ETC_DIR\scala.classpath"
    loadClasspathFromFile $CP_FILE
    $global:__TOOLCHAIN = $global:_CLASS_PATH_RESULT

    $CP_FILE = "$env:_ETC_DIR\with_compiler.classpath"
    loadClasspathFromFile $CP_FILE

    if ($global:_CLASS_PATH_RESULT) {
        $global:__TOOLCHAIN += "$env:_PSEP$global:_CLASS_PATH_RESULT"
    }

    if ($global:_SCALA_CPATH) {
        $global:_JVM_CP_ARGS = "$global:__TOOLCHAIN$global:_SCALA_CPATH"
    } else {
        $global:_JVM_CP_ARGS = $global:__TOOLCHAIN
    }
}

function loadClasspathFromFile {
    param (
        [string]$argFile
    )
    $global:_CLASS_PATH_RESULT = ""
    if (Test-Path $argFile) {
        $lines = Get-Content $argFile
        foreach ($line in $lines) {
            $lib = "$global:_PROG_HOME\maven2\$line".Replace('/', '\')
            if (!$global:_CLASS_PATH_RESULT) {
                $global:_CLASS_PATH_RESULT = $lib
            } else {
                $global:_CLASS_PATH_RESULT += "$env:_PSEP$lib"
            }
        }
    }
}

# Cleanups
end
exit $global:_EXITCODE
