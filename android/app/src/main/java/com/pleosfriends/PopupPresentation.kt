package com.pleosfriends

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Presentation only: the caller owns recognition, validation, dismissal and committing. */
@Composable internal fun GleoInputPresentation(title:String,value:String,listening:Boolean,state:TravelState,
    error:String,helper:String,edit:(String)->Unit,recognize:()->Unit,close:()->Unit,save:()->Unit) {
    val voice=title.contains("음성")
    val heading=when {
        title.contains("친구들에게 전할") -> "친구들한테 음성으로 알려주기"
        title.contains("음성으로 답장") -> "친구에게 음성으로 답장하기"
        else -> title.removePrefix("Gleo AI · ")
    }
    ReferencePopupFrame(close,Modifier.testTag("gleo-input-popup")) {
        Box(Modifier.fillMaxWidth().padding(horizontal=42.dp),contentAlignment=Alignment.Center) {
            PopupText(heading,33,FontWeight.Bold)
        }
        if(voice) {
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp,Alignment.CenterHorizontally)) {
                state.members.forEachIndexed { index,member ->
                    if(member.id!=state.viewerId) Surface(color=Paper,shape=RoundedCornerShape(18.dp)) {
                        Row(Modifier.padding(horizontal=16.dp,vertical=9.dp),verticalAlignment=Alignment.CenterVertically) {
                            Box(Modifier.size(10.dp).background(CarColors[index],CircleShape))
                            Spacer(Modifier.width(9.dp));Label(member.name,21,color=Muted)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(if(voice) 26.dp else 32.dp))
        OutlinedTextField(value,edit,
            modifier=Modifier.fillMaxWidth().heightIn(min=if(voice) 138.dp else 112.dp)
                .border(2.dp,Purple.copy(.85f),RoundedCornerShape(27.dp)).testTag("place-input"),
            placeholder={Label(if(voice) "친구들에게 전할 말을 입력해 주세요" else "직접 입력하거나 Gleo AI로 말해 주세요",25,color=Muted)},
            singleLine=!voice,minLines=if(voice) 2 else 1,maxLines=if(voice) 6 else 1,
            textStyle=textStyle(),shape=RoundedCornerShape(27.dp),
            colors=OutlinedTextFieldDefaults.colors(focusedBorderColor=Color.Transparent,unfocusedBorderColor=Color.Transparent,
                focusedContainerColor=Color.White,unfocusedContainerColor=Color.White),
            trailingIcon={
                Button(recognize,Modifier.padding(horizontal=14.dp,vertical=14.dp).height(61.dp).testTag("gleo"),
                    shape=RoundedCornerShape(24.dp),colors=ButtonDefaults.buttonColors(containerColor=Purple),
                    contentPadding=PaddingValues(horizontal=20.dp)) {
                    Icon(Icons.Rounded.AutoAwesome,null,Modifier.size(22.dp));Spacer(Modifier.width(8.dp))
                    Label(if(listening) "듣고 있어요" else "Gleo AI",22,FontWeight.Bold,Color.White)
                }
            })
        if(!voice) Label(helper,18,color=Muted,modifier=Modifier.fillMaxWidth().padding(top=14.dp))
        if(error.isNotBlank()) Label(error,21,color=Color(0xFFB3261E),modifier=Modifier.padding(top=10.dp))
        Row(Modifier.fillMaxWidth().padding(top=24.dp),horizontalArrangement=Arrangement.spacedBy(16.dp)) {
            Button(close,Modifier.weight(1f).height(88.dp),shape=RoundedCornerShape(25.dp),
                colors=ButtonDefaults.buttonColors(containerColor=Color(0xFFF0F1F4),contentColor=Ink)) {
                Label(if(voice) "보내지 않기" else "닫기",28,FontWeight.Bold)
            }
            Button(save,Modifier.weight(1f).height(88.dp).testTag("input-save"),enabled=value.isNotBlank(),shape=RoundedCornerShape(25.dp),
                colors=ButtonDefaults.buttonColors(containerColor=Purple,disabledContainerColor=Color(0xFFD1C9FF),disabledContentColor=Color.White)) {
                Label(if(voice) "보내기" else "입력 완료",28,FontWeight.Bold,Color.White)
            }
        }
    }
}

/** Existing completion payload, rendered as the centered success card in the reference. */
@Composable internal fun CompletionPresentation(message:String,close:()->Unit) {
    val lines=message.lines().map(String::trim).filter(String::isNotBlank)
    val sent=lines.any { it.contains("친구들에게 알림을 보냈어요") }
    val personal=lines.any { it.startsWith("내 차 경로") }
    val heading=when {
        sent -> "친구들에게 알림을 보냈어요"
        personal -> "내 여행 일정만 수정했어요"
        else -> "전체 여행 일정을 수정했어요"
    }
    val details=lines.drop(2).joinToString("\n")
    ReferencePopupFrame(close,Modifier.testTag("completion-popup")) {
        Box(Modifier.align(Alignment.CenterHorizontally).padding(top=8.dp).size(84.dp).background(Purple,CircleShape),contentAlignment=Alignment.Center) {
            Icon(Icons.Rounded.Check,null,Modifier.size(44.dp),tint=Color.White)
        }
        Spacer(Modifier.height(26.dp))
        PopupText(heading,33,FontWeight.Bold)
        if(!sent && details.isNotBlank()) {
            Spacer(Modifier.height(20.dp))
            PopupText(details,23,FontWeight.SemiBold,Muted)
        }
        Spacer(Modifier.height(30.dp))
        if(sent) {
            Row(Modifier.fillMaxWidth().testTag("completion-confirm").clickable(onClick=close).padding(vertical=12.dp),
                horizontalArrangement=Arrangement.spacedBy(10.dp,Alignment.CenterHorizontally),verticalAlignment=Alignment.CenterVertically) {
                Icon(Icons.Rounded.VolumeUp,"완료 안내 닫기",Modifier.size(26.dp),tint=Purple)
                Label("Gleo AI 안내",24,FontWeight.Bold,Purple)
            }
        } else Button(close,Modifier.fillMaxWidth().height(88.dp).testTag("completion-confirm"),shape=RoundedCornerShape(25.dp),
            colors=ButtonDefaults.buttonColors(containerColor=Purple)) {Label("확인",29,FontWeight.Bold,Color.White)}

    }
}

@Composable private fun ReferencePopupFrame(close:()->Unit,modifier:Modifier,content:@Composable ColumnScope.()->Unit) {
    BackHandler { close() }
    Box(Modifier.fillMaxSize().background(Ink.copy(.24f)).pointerInput(Unit) { detectTapGestures { } },contentAlignment=Alignment.Center) {
        Box(modifier.widthIn(max=1020.dp).fillMaxWidth(.9f).heightIn(max=970.dp)
            .shadow(20.dp,RoundedCornerShape(44.dp)).clip(RoundedCornerShape(44.dp)).background(Color.White)) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal=36.dp,vertical=36.dp),content=content)
            IconButton(close,Modifier.align(Alignment.TopEnd).padding(12.dp).size(44.dp)) {
                Icon(Icons.Rounded.Close,"닫기",Modifier.size(24.dp),tint=Muted)
            }
        }
    }
}

@Composable private fun PopupText(value:String,size:Int,weight:FontWeight,color:Color=Ink) {
    Text(value,Modifier.fillMaxWidth(),color,fontSize=size.sp,fontFamily=TravelReviewFont,fontWeight=weight,
        lineHeight=(size*1.4f).sp,textAlign=TextAlign.Center)
}
