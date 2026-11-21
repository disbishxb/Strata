package dev.cluvex.strata.core.donate

data class Wallet(
    val id: String,
    val network: String,
    val address: String,
    val isLink: Boolean = false,
    val description: String? = null,
    /** 二维码实际编码的内容；为空时回退到 address。 */
    val qrContent: String? = null,
)

object Donation {
    val originalWallets = listOf(
        Wallet("tron", "TRX / USDT · Tron (TRC-20)", "TRxVSHcoADZnBfztFmFb2TQopusAwWYEVR"),
        Wallet("bnb", "USDT · BNB Smart Chain (BEP-20)", "0x20c980AEF36Aa71D0A0e7bE382AeD20b73bC6D27"),
        Wallet("ton", "TON (Gram)", "UQAH75bXaaRUhZMwiF0ZujOXFDDmvLSPASKoOsWF0HNasiaM"),
    )

    val forkWallets = listOf(
        Wallet(
            id = "alipay",
            network = "9008(支付宝)",
            address = "",
            isLink = true,
            description = "请我喝杯咖啡吧！",
            qrContent = "https://qr.alipay.com/fkx194307kvvcomtlpvu67a"
        )
    )
}
