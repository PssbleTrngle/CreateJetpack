# Create Jetpack <!-- modrinth_exclude.start --> <img src="https://raw.githubusercontent.com/PssbleTrngle/CreateJetpack/1.18.x/src/main/resources/assets/create_jetpack/icon.png" align="right" height="128" /> <!-- modrinth_exclude.end -->

Using brass you are able to upgrade your copper backtank to a jetpack, 
using the pressurized air inside to propel yourself through the air.

Inspired by [Simply Jetpacks](https://www.curseforge.com/minecraft/mc-mods/simply-jetpacks-2),
this jetpack also has a hover mode.

Like the copper backtank, the jetpack does also go in the chest slot, 
feeds air to items like the extendo-grip and can be enchanted with _Capacity_.
Just like the backtank it is charged by placing it down and supplying it with rotational force.

![Usage](https://raw.githubusercontent.com/PssbleTrngle/CreateJetpack/1.18.x/screenshots/usage.png)

When underwater while sprint-swimming, the jetpack boosts your swimming speed.
Additionally, the hover-mode prevents you from floating downwards.

![Underwater Usage](https://raw.githubusercontent.com/PssbleTrngle/CreateJetpack/1.18.x/screenshots/underwater.png)

If a mod similar to [Elytra Slot](https://github.com/illusivesoulworks/elytraslot), 
which enables equipping an elytra in addition to a chestplate, 
the jetpack will instead give you firework-like boost while the player is pressing the `UP`-key.

![Elytra Support](https://raw.githubusercontent.com/PssbleTrngle/CreateJetpack/1.18.x/screenshots/elytra.png)

Curios Support is possible by adding it via a datapack. Save to following to `data/curios/tags/items/back.json`:

```json
{
  "replace": false,
  "values": [
    "create_jetpack:jetpack"
  ]
}
```
