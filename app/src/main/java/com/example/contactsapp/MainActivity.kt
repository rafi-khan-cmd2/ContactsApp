package com.example.contactsapp

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import com.example.contactsapp.ui.theme.ContactsAppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.compose.koinViewModel
import org.koin.core.context.startKoin

@Composable
fun Nav() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "TheScreen") {
        composable(
            route = "TheScreen"
        ) {
            TheScreen(navController)
        }
        composable(
            route = "DetailScreen/{theFirstName}/{theLastName}/{thePhoneNumber}/{theEmail}",
            arguments = listOf(
                navArgument(name = "theFirstName") {
                    type = NavType.StringType
                },
                navArgument(name = "theLastName") {
                    type = NavType.StringType
                },
                navArgument(name = "thePhoneNumber") {
                    type = NavType.StringType
                },
                navArgument(name = "theEmail") {
                    type = NavType.StringType
                }
            )) { backstackEntry ->
            val firstName = backstackEntry.arguments?.getString("theFirstName")
            val lastName = backstackEntry.arguments?.getString("theLastName")
            val phoneNumber = backstackEntry.arguments?.getString("thePhoneNumber")
            val email = backstackEntry.arguments?.getString("theEmail")
            ContactDetail(
                navController,firstName, lastName, phoneNumber, email
            )
        }
    }
}
@Entity(tableName = "ContactTable")
data class Contact(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String? = null
)

@Dao
interface ContactDao{
    @Query("SELECT * FROM ContactTable ORDER BY firstName ASC")
    fun getAllContacts(): Flow<List<Contact>>

    @Insert
    suspend fun insertContact(contact: Contact)

    @Delete
    suspend fun deleteContact(contact: Contact)
}

@Database(entities = [Contact::class], version = 1)
abstract class ContactDatabase: RoomDatabase() {
    abstract fun contactDao(): ContactDao
}

class ContactRepository(private val contactDao:ContactDao){
    fun getAllContacts():Flow<List<Contact>> = contactDao.getAllContacts()

    suspend fun insertContact(contact: Contact) {
        contactDao.insertContact(contact)
    }

    suspend fun deleteContact(contact:Contact) {
        contactDao.deleteContact(contact)
    }

}

class TheViewModel(private val Repo: ContactRepository): ViewModel() {
    val allContacts: Flow<List<Contact>> = Repo.getAllContacts()

    fun insert(contact:Contact) = viewModelScope.launch {
        Repo.insertContact(contact)
    }

    fun delete(contact:Contact) = viewModelScope.launch {
        Repo.deleteContact(contact)
    }
}

@Composable
fun TheScreen(navController: NavController,theViewModel:TheViewModel = koinViewModel()){
    val contacts by theViewModel.allContacts.collectAsState(initial = emptyList())

    var firstName by remember {mutableStateOf("")}
    var lastName by remember {mutableStateOf("")}
    var phoneNumber by remember {mutableStateOf("")}
    var email by remember {mutableStateOf("")}

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        OutlinedTextField(
            value = firstName,
            onValueChange = {firstName = it},
            label = {Text("Enter the first name: ")},
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = lastName,
            onValueChange = {lastName = it},
            label = {Text("Enter the last name: ")},
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = {phoneNumber = it},
            label = {Text("Enter the phone number: ")},
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = email,
            onValueChange = {email = it},
            label = {Text("Enter the email: ")},
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,   // Default elevation
                pressedElevation = 12.dp,  // Elevation when pressed
                hoveredElevation = 4.dp,   // Elevation when hovered
                focusedElevation = 6.dp    // Elevation when focused
            ),
            onClick = {
                theViewModel.insert(Contact(
                    firstName = firstName,
                    lastName = lastName,
                    phoneNumber = phoneNumber,
                    email = email)
                )
                firstName = ""
                lastName = ""
                phoneNumber = ""
                email = ""
            },
            modifier = Modifier.padding(top = 16.dp)) {
            Text("Add Contact")
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(contacts) {
                    contact ->
                ContactItem(navController,contact = contact, onDeleteClick = {theViewModel.delete(contact)})
            }

        }

    }

}

@Composable
fun ContactItem(navController:NavController,contact:Contact, onDeleteClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable{
                navController.navigate("DetailScreen/${contact.firstName}/${contact.lastName}/${contact.phoneNumber}/${contact.email}")
            },
        horizontalArrangement= Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "${contact.firstName} ${contact.lastName}")
        Text(text = contact.phoneNumber)
    }
    IconButton(onClick = {onDeleteClick()}) {
        Icon(Icons.Default.Delete, contentDescription = "Delete Contact")
    }
}

@Composable
fun ContactDetail(navController: NavController,firstName: String?, lastName: String?, phoneNumber: String?, email: String?){
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        //Text("Details Screen", fontSize = 54.sp)
        //Spacer(modifier = Modifier.height(45.dp))
        Text(text = "Name: $firstName $lastName",fontSize = 20.sp)
        Spacer(modifier = Modifier.height(25.dp))
        Text(text = "Phone Number: $phoneNumber", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(25.dp))
        Text(text = "Email: $email", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(25.dp))
    }
    Button(
        modifier = Modifier.absoluteOffset(x = 10.dp, y = 800.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,   // Default elevation
            pressedElevation = 12.dp,  // Elevation when pressed
            hoveredElevation = 4.dp,   // Elevation when hovered
            focusedElevation = 6.dp    // Elevation when focused
        ),
        onClick = {
            navController.navigate("TheScreen")
        }
    ) {
        Text(text = "Back to contacts")
    }

}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ContactsAppTheme {
                //TheScreen()
                Nav()
            }
        }
    }
}

class TheApp: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@TheApp)
            modules(appModule)
        }
    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ContactsAppTheme {
        Nav()
    }
}