package com.example.ca3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.room3.Dao
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import androidx.room3.Query
import com.example.ca3.ui.theme.Ca3Theme
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val email: String
)

@Dao
interface StudentDao {

    @Query("SELECT * FROM students")
    fun getAllStudents(): Flow<List<StudentEntity>>
}

class StudentViewModel(
    private val dao: StudentDao
) : ViewModel() {

    val students: Flow<List<StudentEntity>> =
        dao.getAllStudents()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudentScreen()
        }
    }
}

@Composable
fun StudentScreen(
    viewModel: StudentViewModel = ViewModel()
) {
    val students by viewModel.students.collectAsState(
        initial = emptyList()
    )

    StudentList(students = students)
}

@Composable
fun StudentList(
    students: List<StudentEntity>
) {
    LazyColumn {
        items(students) { student ->
            Text(
                text = "${student.name} - ${student.email}",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}