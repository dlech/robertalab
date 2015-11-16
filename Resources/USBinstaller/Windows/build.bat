candle lib.wxs java.wxs setup.wxs
light -out OpenRobertaEV3.msi -ext WixUIExtension -cultures:de-DE lib.wixobj java.wixobj setup.wixobj -b ./lib -b ./java
@pause