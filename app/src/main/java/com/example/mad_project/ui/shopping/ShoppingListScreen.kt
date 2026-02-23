import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.tooling.preview.Preview
import com.example.mad_project.ui.components.AppTopBar
import com.example.mad_project.ui.components.EmptyView
import com.example.mad_project.ui.theme.MADProjectTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = { AppTopBar(title = "Shopping List", showBack = true, onBackClick = onBackClick) }
    ) { padding ->
        EmptyView("Shopping list empty")
    }
}

@Preview(showBackground = true)
@Composable
private fun ShoppingListScreenPreview() {
    MADProjectTheme(dynamicColour = false) {
        ShoppingListScreen(onBackClick = {})
    }
}
