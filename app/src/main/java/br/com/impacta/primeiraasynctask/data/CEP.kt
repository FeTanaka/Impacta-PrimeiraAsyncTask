package br.com.impacta.primeiraasynctask.data

data class CEP(
    val bairro: String,
    val cep: String,
    val cidade: String,
    val cidade_info: CidadeInfo,
    val complemento: String? = null,
    val estado: String,
    val estado_info: EstadoInfo,
    val logradouro: String
)