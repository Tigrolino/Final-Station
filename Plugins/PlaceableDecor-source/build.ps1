param(
    # Folder containing Paper's dependency JARs, including paper-api and JOML.
    [string]$Libraries = (Join-Path $PSScriptRoot '../../test/pikmin-build')
)

$ErrorActionPreference = 'Stop'
$dependencies = @(Get-ChildItem -LiteralPath $Libraries -Recurse -Filter '*.jar' -File |
    Where-Object { $_.Name -ne 'mojang_26.3.jar' } | Select-Object -ExpandProperty FullName)
if ($dependencies.Count -eq 0) { throw 'No Paper dependency JARs found. Use Maven or specify -Libraries.' }
$classes = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot 'target/classes'))
if (-not $classes.StartsWith([IO.Path]::GetFullPath($PSScriptRoot) + [IO.Path]::DirectorySeparatorChar)) {
    throw 'Unexpected generated classes directory'
}
if (Test-Path -LiteralPath $classes) { Remove-Item -LiteralPath $classes -Recurse -Force }
New-Item -ItemType Directory -Force -Path $classes | Out-Null
$sources = @(Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot 'src/main/java') -Recurse -Filter '*.java' |
    Select-Object -ExpandProperty FullName)
& javac --release 25 -encoding UTF-8 -classpath ($dependencies -join [IO.Path]::PathSeparator) -d $classes @sources
if ($LASTEXITCODE -ne 0) { throw 'Java compilation failed' }
Copy-Item -Path (Join-Path $PSScriptRoot 'src/main/resources/*') -Destination $classes -Force
$output = Join-Path $PSScriptRoot 'target/PlaceableDecor-1.1.0.jar'
& jar --create --file $output -C $classes .
if ($LASTEXITCODE -ne 0) { throw 'JAR packaging failed' }
Write-Output $output
