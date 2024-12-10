package com.example.timer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Timer

class TelaRecentTimers : AppCompatActivity() {
    lateinit var voltar_btn: ImageButton
    lateinit var listTimer: ListView
    lateinit var databaseReference: DatabaseReference

    private val ListaTimers = mutableListOf<TimerData>()
    private lateinit var adapter: ArrayAdapter<TimerData>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tela_recent_timers)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        voltar_btn = findViewById(R.id.voltar_btn)

        listTimer = findViewById(R.id.ListTimer)

        voltar_btn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // INICIALIZAR O FIREBASE
        databaseReference = FirebaseDatabase.getInstance("https://timerkotlinapk-default-rtdb.firebaseio.com/").reference.child("Timers")

        adapter = object : ArrayAdapter<TimerData>(this, android.R.layout.simple_list_item_1, ListaTimers) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val itemView = super.getView(position, convertView, parent)
                val timerItem = getItem(position)

                itemView.findViewById<TextView>(android.R.id.text1).text = "${timerItem?.nome} - ${timerItem?.timer}"
                return itemView
            }
        }
        listTimer.adapter = adapter

        // Carregar os dados do firebase
        CarregarTimerFirebase()

        listTimer.setOnItemClickListener { _, _, position, _ ->
            val TimerSelecionado = ListaTimers[position]
            //Toast.makeText(this, "Selecionado: ${TimerSelecionado}", Toast.LENGTH_SHORT).show()
            DeletarIniciarTimer(TimerSelecionado.id, TimerSelecionado.nome)
        }

    }

    private fun CarregarTimerFirebase() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                ListaTimers.clear()
                for (timerSnapshot in snapshot.children) {
                    val id = timerSnapshot.key.toString()
                    val nome = timerSnapshot.child("nome").value.toString()
                    val tempo = timerSnapshot.child("timer").value.toString()
                    ListaTimers.add(TimerData(id, nome, tempo))
                }
                adapter.notifyDataSetChanged() // ATUALIZA O LISTVIEW
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TelaRecentTimers, "Erro ao carregar dados: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    //************************************************************************
    // ******************* MÉTODO DELETAR/INICIAR TIMER **********************
    //************************************************************************

    fun DeletarIniciarTimer(id: String, nome: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Escolha uma opção")
        builder.setMessage("O que deseja fazer com este timer: '$nome'?")

        // CASO O USUÁRIO DECIDA INICIAR O TIMER
        // SEM IMPLEMENTAÇÃO
        builder.setPositiveButton("Iniciar")  {_, _ ->
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

        }

        // CASO O USUÁRIO DESEJE EXCLUIR O TIMER
        builder.setNegativeButton("Excluir") { dialog, _ ->
            databaseReference.child(id).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Timer removido com sucesso!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Erro ao remover o timer: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }
}