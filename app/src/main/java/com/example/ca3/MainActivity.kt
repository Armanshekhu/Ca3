package com.example.ca3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room3.Dao
import androidx.room3.Database
import androidx.room3.Entity
import androidx.room3.Insert
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

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

    @Insert
    suspend fun insert(student: StudentEntity)
}

@Database(entities = [StudentEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
}

class StudentViewModel(
    private val dao: StudentDao
) : ViewModel() {

    val students: Flow<List<StudentEntity>> = dao.getAllStudents()

    // Optional: lets you see live updates while testing
    fun addSample() {
        viewModelScope.launch {
            dao.insert(StudentEntity(name = "Student ${System.nanoTime() % 100}", email = "test@example.com"))
        }
    }
}

class StudentViewModelFactory(private val dao: StudentDao) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        StudentViewModel(dao) as T
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = Room.databaseBuilder<AppDatabase>(
            context = applicationContext,
            name = "student_db"
        )
            .setDriver(BundledSQLiteDriver())
            .build()

        setContent {
            StudentScreen(
                viewModel = viewModel(
                    factory = StudentViewModelFactory(db.studentDao())
                )
            )
        }
    }
}

@Composable
fun StudentScreen(viewModel: StudentViewModel) {
    val students by viewModel.students.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )
    StudentList(students = students)
}

@Composable
fun StudentList(students: List<StudentEntity>) {
    LazyColumn {
        items(students) { student ->
            Text(
                text = "${student.name} - ${student.email}",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}