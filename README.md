# HealthHider

HealthHider allows you to hide the health of other entities, to prevent players with cheat clients or health indicator
mods from gaining an unfair advantage. The plugin can be configured to only hide the health of certain entities. For
example, you could only hide the health for other player entities, while allowing your users to use health indicators on
regular mobs.

![Demo](demo.gif)

*Demonstration GIF showing the pig's health always being displayed as 1, despite it being different*

> **Note:** HealthHider breaks mods that rely on the health value, such as entity healthbar mods or damage indicators!

### Configuration

```yml
# HealthHider

# Whether the plugin should check the 'healthider.bypass' on players.
# When this setting is enabled and the player has the permission, their client will be sent real healths.
enable-bypass-permission: false

# Which mode the 'entities' list below should operate in.
# When set to blacklist, all living entities will have their healths hidden EXCEPT for the entities listed.
# When set to whitelist, only the listed living entities will have their healths hidden.
list-mode: blacklist

# The list of entities to show or hide the health of, depending on the mode set above.
# Note that not all entities have health!
entities:
    - iron_golem # iron golems crack when they have low health
    - wolf # wolves move their tails depending on their health
```

<details>
<summary>bStats</summary>

![bStats Graph](https://bstats.org/signatures/bukkit/HealthHider.svg)
</details>
