package tech.thatgravyboat.skyblockpv.data

enum class SkullTextures(val texture: String) {
    WITHER_ESSENCE("ewogICJ0aW1lc3RhbXAiIDogMTYwMzYxMDQ0MzU4MywKICAicHJvZmlsZUlkIiA6ICIzM2ViZDMyYmIzMzk0YWQ5YWM2NzBjOTZjNTQ5YmE3ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJEYW5ub0JhbmFubm9YRCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9lNDllYzdkODJiMTQxNWFjYWUyMDU5Zjc4Y2QxZDE3NTRiOWRlOWIxOGNhNTlmNjA5MDI0YzRhZjg0M2Q0ZDI0IgogICAgfQogIH0KfQ\u003d\u003d"),
    SPIDER_ESSENCE("eyJ0aW1lc3RhbXAiOjE0OTY4MTAwOTAwMDMsInByb2ZpbGVJZCI6IjdkYTJhYjNhOTNjYTQ4ZWU4MzA0OGFmYzNiODBlNjhlIiwicHJvZmlsZU5hbWUiOiJHb2xkYXBmZWwiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzE2NjE3MTMxMjUwZTU3ODMzM2E0NDFmZGY0YTViOGM2MjE2MzY0MGE5ZDA2Y2Q2N2RiODkwMzFkMDNhY2NmNiJ9fX0\u003d"),
    UNDEAD_ESSENCE("ewogICJ0aW1lc3RhbXAiIDogMTcwODc3MzM5OTY2MCwKICAicHJvZmlsZUlkIiA6ICJmZmU5MzczY2YyMDM0OWFhYTJlN2NiYzJkZmY2M2I5MyIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWxvblR1bmExIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzY1ZDlkOThhZDZkMTU0ZWYxZjRjNjNkYmE5MmE1MTMzN2NkMWY0ZWIwY2I2NWI5YjJhOTBmZThiMDMwOTZkYjciLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ\u003d\u003d"),
    DRAGON_ESSENCE("ewogICJ0aW1lc3RhbXAiIDogMTY3NjkwMDk5NTYzMCwKICAicHJvZmlsZUlkIiA6ICJmZWYyZDZjYzY5ZGI0ZWM5OWQzYzI5MzBmYzRmNTBhYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJsb3Zlbm90d2FyIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzMzZmY0MTZhYThiZWMxNjY1YjkyNzAxZmJlNjhhNGVmZmZmM2QwNmVkOTE0NzQ1NGZhNzc3MTJkZDYwNzliMzMiCiAgICB9CiAgfQp9"),
    GOLD_ESSENCE("ewogICJ0aW1lc3RhbXAiIDogMTY4MDYyMDk2OTk0MSwKICAicHJvZmlsZUlkIiA6ICJkYmE4OTUzOThiYTc0MzZlOTQ2YzVkZTk4N2UzZGVkNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTb21lQ29tbW9uIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2M1OWVlNDcxNDM1ZGMzYzEzMDNhMDA2NjVkOGIyOTNhZjRlMGIyMjhjNTNjZDUzYmYwMzlhMmE3Mzk0ZjdjNmMiCiAgICB9CiAgfQp9"),
    DIAMOND_ESSENCE("ewogICJ0aW1lc3RhbXAiIDogMTYyODU0MDAzMDA0OSwKICAicHJvZmlsZUlkIiA6ICJiN2ZkYmU2N2NkMDA0NjgzYjlmYTllM2UxNzczODI1NCIsCiAgInByb2ZpbGVOYW1lIiA6ICJDVUNGTDE0IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzVlZTY5YTA3Zjc1N2FhOGE3MjU2NjA2ZGQ5ZWMwYjUzY2E2OGUzOTExMWViZTEyMzcyZGE3ZGQxNWE4N2IwZSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9"),
    ICE_ESSENCE("ewogICJ0aW1lc3RhbXAiIDogMTYxOTg1NTE0OTQ4NCwKICAicHJvZmlsZUlkIiA6ICJjZGM5MzQ0NDAzODM0ZDdkYmRmOWUyMmVjZmM5MzBiZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJSYXdMb2JzdGVycyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS83ZmQ1ZjZiNmRiOTNkNTMwMjRmZWYyNzk4ZWI3ZThjYTQxMWEyYzZhZTEzMjA5YWQxM2UyOWZiOTgwZjE3ZjUiCiAgICB9CiAgfQp9"),
    CRIMSON_ESSENCE("ewogICJ0aW1lc3RhbXAiIDogMTY0NDc4ODQzMzE5NSwKICAicHJvZmlsZUlkIiA6ICIxZjEyNTNhYTVkYTQ0ZjU5YWU1YWI1NmFhZjRlNTYxNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJOb3RNaUt5IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzY3YzQxOTMwZjhmZjBmMmIwNDMwZTE2OWFlNWYzOGU5ODRkZjEyNDQyMTU3MDVjNmYxNzM4NjI4NDQ1NDNlOWQiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ\u003d\u003d"),
    BACKPACK("eyJ0aW1lc3RhbXAiOjE1NjgyMTMwNTE0MjYsInByb2ZpbGVJZCI6IjgyYzYwNmM1YzY1MjRiNzk4YjkxYTEyZDNhNjE2OTc3IiwicHJvZmlsZU5hbWUiOiJOb3ROb3RvcmlvdXNOZW1vIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS82MmYzYjNhMDU0ODFjZGU3NzI0MDAwNWMwZGRjZWUxYzA2OWU1NTA0YTYyY2UwOTc3ODc5ZjU1YTM5Mzk2MTQ2In19fQ\u003d\u003d"),
    ACCESSORY_BAG("ewogICJ0aW1lc3RhbXAiIDogMTYwMDYxNTg5ODc1MCwKICAicHJvZmlsZUlkIiA6ICJhNzdkNmQ2YmFjOWE0NzY3YTFhNzU1NjYxOTllYmY5MiIsCiAgInByb2ZpbGVOYW1lIiA6ICIwOEJFRDUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWExMWE3ZjExYmNkNTc4NDkwM2M1MjAxZDA4MjYxYzRkZjgzNzkxMDlkNmU2MTFjMWNkM2VkZWRmMDMxYWZlZCIKICAgIH0KICB9Cn0\u003d")
    ;

    fun createSkull() = tech.thatgravyboat.skyblockpv.utils.createSkull(texture)
}
