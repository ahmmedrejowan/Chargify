package com.rejowan.chargify.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CardTitle(
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp),
    title: String = "",
    leadingIcon: Int,
    trailingIcon: Int? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = leadingIcon),
            contentDescription = title,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .size(16.dp)
        )

        Text(
            text = title,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
            textAlign = TextAlign.Start
        )

        trailingIcon?.let {
            Icon(
                painter = painterResource(id = it),
                contentDescription = "Arrow",
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(16.dp)
                    .clickable { onClick() }
            )
        }
    }
}
