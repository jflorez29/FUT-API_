package ea.sports.fut.model

enum class Platform(val sku : String, val host : String, val platPin : String) {

    PC("PCC", "utas.external.s2.fut.ea.com:443", "pc"),
    PS4("PS4", "utas.external.s2.fut.ea.com:443", "ps4"),
    XBOXONE("XBO", "utas.external.s3.fut.ea.com:443", "xbox")
}