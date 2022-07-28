package br.com.impacta.primeiraasynctask

import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import br.com.impacta.primeiraasynctask.data.CEP
import br.com.impacta.primeiraasynctask.data.CidadeInfo
import br.com.impacta.primeiraasynctask.data.EstadoInfo
import br.com.impacta.primeiraasynctask.databinding.FragmentHomeBinding
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.max


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.button.setOnClickListener {
            if (binding.editTextTextPersonName.text.isNotEmpty()) {
                val asyncTask = ConsultaCEP(10)
                asyncTask.execute(binding.editTextTextPersonName.text.toString())
            }
        }
    }

    inner class ConsultaCEP(val maximo: Int = 10): AsyncTask<String, Int, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
            binding.progressBar.max = maximo
            binding.progressBar.progress = 0
        }

        override fun doInBackground(vararg p0: String?): String {
            val apiUrl = "https://api.postmon.com.br/v1/cep/${p0[0]}"
            val url = URL(apiUrl)
            var cep: CEP? = null
            var error: String = ""

            (url.openConnection() as? HttpURLConnection)?.let { conexao ->
                conexao.requestMethod = "GET"
                conexao.connectTimeout = 15000
                conexao.readTimeout = 15000
                conexao.connect()

                val inputStream = if (conexao.responseCode == HttpURLConnection.HTTP_OK) {
                    conexao.inputStream
                } else {
                    conexao.errorStream
                }

                val resposta = inputStream.reader().readText()

                if (conexao.responseCode == HttpURLConnection.HTTP_OK) {
                    val json = JSONObject(resposta)
                    val bairro = json.getString("bairro")
                    val cepJSON = json.getString("cep")
                    val cidade = json.getString("cidade")
                    val estado = json.getString("estado")
                    val logradouro = json.getString("logradouro")
                    val cidadeInfo = json.getJSONObject("cidade_info")
                    val cidadeInfoArea = cidadeInfo.getString("area_km2")
                    val cidadeInfoCodigo = cidadeInfo.getString("codigo_ibge")
                    val estadoInfo = json.getJSONObject("estado_info")
                    val estadoInfoArea = estadoInfo.getString("area_km2")
                    val estadoInfoCodigo = estadoInfo.getString("codigo_ibge")
                    val estadoInfoNome = estadoInfo.getString("nome")
                    val complemento: String? = try {
                        json.getString("complemento")
                    }catch (e: Exception) {
                        null
                    }

                    val cidadeInfoObj = CidadeInfo(cidadeInfoArea, cidadeInfoCodigo)
                    val estadoInfoObj = EstadoInfo(estadoInfoArea, estadoInfoCodigo, estadoInfoNome)
                    cep = CEP(bairro, cepJSON, cidade, cidadeInfoObj, complemento, estado, estadoInfoObj, logradouro)
                } else {
                    error = resposta
                }
            }

            for (i in 0..maximo) {
                Thread.sleep(1500)
                publishProgress(i)
            }

            return if (cep == null) {
                error
            } else {
                cep.toString()
            }
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            binding.progressBar.progress = values[0]!!
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            binding.textView.text = result.toString()
        }
    }

}