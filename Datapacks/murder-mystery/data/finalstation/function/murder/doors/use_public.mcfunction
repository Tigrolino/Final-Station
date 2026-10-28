# as a public door: works like a normal door (no lock, no auto-close)
execute positioned ~ ~0.1 ~ align xyz if block ~ ~ ~ #minecraft:doors[open=true] run return run function finalstation:murder/doors/set {open:"false",act:"close"}
execute positioned ~ ~0.1 ~ align xyz run function finalstation:murder/doors/set {open:"true",act:"open"}
