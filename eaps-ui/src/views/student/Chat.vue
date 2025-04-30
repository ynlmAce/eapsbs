<template>
  <div class="chat-container">
    <div class="chat-sidebar">
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索会话"
          prefix-icon="el-icon-search"
          clearable
        />
        <el-button
          type="primary"
          size="small"
          style="margin-top: 10px; width: 100%;"
          :loading="contactingCounselor"
          @click="contactCounselor"
        >联系辅导员</el-button>
      </div>

      <div class="session-list">
        <div v-if="loadingSessions" class="sessions-loading">
          <el-skeleton animated :rows="3" />
        </div>
        <div v-else-if="sessions.length === 0" class="empty-sessions">
          <el-empty description="暂无会话" />
          <el-button
            class="refresh-button"
            @click="refreshSessions"
            type="primary"
            size="small"
          >
            刷新会话列表
          </el-button>
        </div>
        <template v-else>
          <div
            v-for="session in filteredSessions"
            :key="session.id"
            class="session-item"
            :class="{
              active: currentSession && currentSession.id === session.id,
            }"
            @click="selectSession(session)"
          >
            <el-badge
              :value="session.unreadCount"
              :hidden="!session.unreadCount"
              class="session-badge"
            >
              <el-avatar
                :size="40"
                :src="getSessionAvatar(session)"
              ></el-avatar>
            </el-badge>
            <div class="session-info">
              <div class="session-header">
                <span class="session-name">{{
                  getSessionName(session)
                }}</span>
                <span class="session-time">{{
                  formatTime(session.lastMessageTime)
                }}</span>
              </div>
              <div class="session-message">{{ session.lastMessage }}</div>
            </div>
          </div>
          <div class="session-actions">
            <el-button
              class="refresh-button"
              @click="refreshSessions"
              type="primary"
              size="small"
              text
            >
              刷新会话列表
            </el-button>
          </div>
        </template>
      </div>
    </div>

    <div class="chat-main">
      <template v-if="currentSession">
        <div class="chat-header">
          <div class="header-title">
            {{ getSessionName(currentSession) }}
            <el-tag
              v-if="
                currentSession.type === 'company' && currentSession.jobTitle
              "
              size="small"
            >
              {{ currentSession.jobTitle }}
            </el-tag>
          </div>
          <div class="header-actions">
            <el-button size="small" @click="showSessionInfo = true"
              >查看信息</el-button
            >
          </div>
        </div>

        <div class="chat-messages" ref="messagesContainer">
          <!-- 错误状态显示 -->
          <div v-if="chatError" class="chat-error-container">
            <el-alert
              title="加载失败"
              :description="errorMessage"
              type="error"
              show-icon
              center
              :closable="false"
            />
            <el-button class="retry-button" type="primary" @click="retryLoad"
              >重试</el-button
            >
          </div>

          <div v-else-if="currentMessages.length === 0" class="empty-messages">
            <el-empty description="没有聊天记录" />
          </div>

          <div
            v-else
            v-for="(message, index) in currentMessages"
            :key="index"
            class="message-wrapper"
          >
            <div class="message-time" v-if="showMessageTime(message, index)">
              {{ formatFullTime(message.sentAt) }}
            </div>
            <div
              class="message"
              :class="{
                'message-self': message.isSelf,
                'message-other': !message.isSelf,
              }"
            >
              <el-avatar
                v-if="!message.isSelf"
                :size="30"
                :src="message.avatar || getMessageAvatar(message)"
                class="message-avatar"
              ></el-avatar>
              <div class="message-content">
                <template v-if="message.contentType === 'text'">
                  <div class="message-text">{{ message.content }}</div>
                </template>

                <template v-else-if="message.contentType === 'image'">
                  <div class="message-image">
                    <el-image
                      :src="message.content"
                      :preview-src-list="[message.content]"
                      fit="cover"
                      lazy
                    ></el-image>
                  </div>
                </template>

                <template v-else-if="message.contentType === 'file'">
                  <div class="message-file">
                    <i class="el-icon-document"></i>
                    <a :href="message.content" target="_blank">{{
                      message.fileName || "文件"
                    }}</a>
                  </div>
                </template>
              </div>
              <el-avatar
                v-if="message.isSelf"
                :size="30"
                :src="message.avatar || currentUserAvatar"
                class="message-avatar"
              ></el-avatar>
            </div>
          </div>

        </div>

        <div class="chat-input">
          <div class="input-toolbar">
            <el-tooltip content="发送图片" placement="top">
              <el-button size="small" @click="triggerImageUpload">
                <i class="el-icon-picture"></i>
              </el-button>
            </el-tooltip>
            <el-tooltip content="发送文件" placement="top">
              <el-button size="small" @click="triggerFileUpload">
                <i class="el-icon-document"></i>
              </el-button>
            </el-tooltip>
            <input
              type="file"
              ref="imageInput"
              accept="image/*"
              style="display: none"
              @change="handleImageUpload"
            />
            <input
              type="file"
              ref="fileInput"
              style="display: none"
              @change="handleFileUpload"
            />
          </div>

          <el-input
            v-model="messageInput"
            type="textarea"
            :rows="3"
            placeholder="输入消息..."
            resize="none"
            @keydown.enter.exact.prevent="sendTextMessage"
          />

          <div class="input-actions">
            <el-button
              type="primary"
              @click="sendTextMessage"
              :disabled="!messageInput.trim()"
            >
              发送
            </el-button>
          </div>
        </div>
      </template>

      <div v-else class="no-session">
        <el-empty description="选择一个会话开始聊天"></el-empty>
        <el-button
          v-if="sessions.length > 0"
          class="retry-button"
          type="primary"
          @click="autoSelectFirstSession"
        >
          自动选择会话
        </el-button>
      </div>
    </div>

    <!-- 会话信息侧栏 -->
    <el-drawer
      v-model="showSessionInfo"
      title="会话信息"
      size="30%"
      :before-close="handleCloseSessionInfo"
      direction="rtl"
    >
      <div v-if="currentSession" class="session-detail">
        <div class="detail-header">
          <el-avatar
            :size="80"
            :src="getSessionAvatar(currentSession)"
          ></el-avatar>
          <h3>{{ getSessionName(currentSession) }}</h3>
          <el-tag size="small">{{
            getSessionTypeText(currentSession.type)
          }}</el-tag>
        </div>

        <template v-if="currentSession.type === 'company'">
          <div class="detail-item">
            <span class="detail-label">企业名称：</span>
            <span class="detail-value">{{ currentSession.name }}</span>
          </div>

          <div v-if="currentSession.jobTitle" class="detail-item">
            <span class="detail-label">相关岗位：</span>
            <span class="detail-value">{{ currentSession.jobTitle }}</span>
          </div>
        </template>

        <template v-else-if="currentSession.type === 'counselor'">
          <div class="detail-item">
            <span class="detail-label">辅导员姓名：</span>
            <span class="detail-value">{{ currentSession.name }}</span>
          </div>
        </template>

        <div class="detail-actions">
          <el-popconfirm
            title="确定要删除此会话吗？"
            @confirm="deleteSession(currentSession.id)"
          >
            <template #reference>
              <el-button type="danger" plain>删除会话</el-button>
            </template>
          </el-popconfirm>

          <el-popconfirm
            title="确定要举报此会话吗？"
            @confirm="reportSession(currentSession.id)"
          >
            <template #reference>
              <el-button
                v-if="currentSession.type === 'company'"
                type="warning"
                plain
                >举报会话</el-button
              >
            </template>
          </el-popconfirm>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import {
  ref,
  computed,
  onMounted,
  nextTick,
  watch,
  onBeforeUnmount,
} from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage, ElLoading, ElMessageBox } from "element-plus";
// 导入API函数
import {
  getChatSessions,
  getChatMessages,
  sendChatMessage,
  markSessionRead,
  uploadChatFile,
  reportChatMessage,
  createStudentCounselorSession,
} from "@/api/chat";
import { getStudentProfile } from "@/api/student";
import { useUserStore } from "@/store/user";

// 添加全局应急恢复函数
window.__chatEmergencyRecover = function () {
  try {
    console.warn("启动聊天应急恢复程序");
    const app = document.querySelector(".__vue_app__")?.__vue__;
    if (!app) return false;

    const chat = app.$root?.$?.subTree?.component?.proxy;
    if (!chat) return false;

    // 重新加载会话列表
    if (typeof chat.loadSessions === "function") {
      chat.loadSessions(true);
      console.log("已触发会话列表重新加载");
    }

    // 如果有会话数据，选择第一个
    if (
      chat.sessions &&
      chat.sessions.length > 0 &&
      typeof chat.selectSession === "function"
    ) {
      setTimeout(() => {
        chat.selectSession(chat.sessions[0]);
        console.log("已选择第一个会话");
      }, 500);
    }

    return true;
  } catch (e) {
    console.error("应急恢复失败:", e);
    return false;
  }
};

// 全局预防表情组件错误的代码
if (!window.__EMOJI_FIXED__) {
  window.__EMOJI_FIXED__ = true;
  // 预定义表情相关对象，防止其他地方访问时出错
  window.EmojiPanel = window.EmojiPanel || {
    init: function () {
      console.log("模拟表情面板初始化");
      return null;
    },
  };
  if (!window.__EMOJI_DATA__) {
    window.__EMOJI_DATA__ = { initialized: true };
  }
  if (window.__EMOJI_DATA__ && typeof window.__EMOJI_DATA__ === 'object') {
    window.__EMOJI_DATA__.exmid = "fixed-value";
  }
  console.log("已安装全局表情组件替代品");
}

// 安装全局错误监听
const installGlobalErrorRecovery = () => {
  // 监听全局未捕获错误
  window.addEventListener(
    "error",
    (event) => {
      console.log("捕获到全局错误:", event.message);

      // 判断是否为渲染相关错误
      if (
        event.message?.includes("render") ||
        event.message?.includes("undefined") ||
        event.message?.includes("null") ||
        event.message?.includes("exmid")
      ) {
        console.warn("检测到可能影响渲染的错误，准备自动恢复");
        event.preventDefault();

        // 延迟执行以避免连锁错误
        setTimeout(() => {
          // 如果加载状态卡住，重置它
          loadingSessions.value = false;

          // 重新加载会话列表
          loadSessions(true);
        }, 1000);

        return true;
      }
    },
    true
  );

  // 监听未处理的Promise错误
  window.addEventListener("unhandledrejection", (event) => {
    console.warn("未处理的Promise错误:", event.reason);

    // 如果是网络请求错误，考虑重新加载
    if (
      event.reason?.message?.includes("network") ||
      event.reason?.message?.includes("timeout") ||
      event.reason?.message?.includes("abort")
    ) {
      // 延迟执行恢复操作
      setTimeout(() => {
        if (!currentSession.value && sessions.value.length > 0) {
          // 如果没有当前会话但有会话列表，选择第一个
          autoSelectFirstSession();
        } else if (sessions.value.length === 0) {
          // 如果会话列表为空，重新加载
          loadSessions(true);
        }
      }, 1000);
    }
  });
};

// 修复函数 - 在外部表情组件出现问题时调用
const fixEmojiComponent = () => {
  try {
    // 尝试初始化表情包数据
    if (window.__EMOJI_DATA__ && !window.__EMOJI_DATA__.initialized) {
      window.__EMOJI_DATA__.initialized = true;
      if (window.__EMOJI_DATA__) {
        window.__EMOJI_DATA__.exmid = "emoji-fixed";
      }
      // 防止错误传播 - 替换可能出错的函数
      if (window.EmojiPanel) {
        const origInit = window.EmojiPanel.init;
        window.EmojiPanel.init = function (...args) {
          try {
            return origInit.apply(this, args);
          } catch (e) {
            console.warn("已拦截表情面板初始化错误:", e);
            return null;
          }
        };
      }
    }
    return true;
  } catch (e) {
    console.warn("修复表情组件失败:", e);
    return false;
  }
};

// 路由和状态管理
const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const searchKeyword = ref("");
const sessions = ref([]);
const loadingSessions = ref(false);
const currentSession = ref(null);
const currentMessages = ref([]);
const messageInput = ref("");
const showSessionInfo = ref(false);
const hasMoreMessages = ref(false);
const loadingMore = ref(false);
const messagesContainer = ref(null);
const imageInput = ref(null);
const fileInput = ref(null);
const loadRetryCount = ref(0);
const MAX_RETRIES = 3;

// 错误处理状态
const chatError = ref(false);
const errorMessage = ref("");

// 添加自动重连定时器
let autoRetryTimer = null;

// 当前用户信息
const currentUserId = computed(() => userStore.userId || 0);
const currentUserAvatar = computed(
  () =>
    userStore.userAvatar ||
    "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png"
);

// 筛选后的会话列表
const filteredSessions = computed(() => {
  if (!searchKeyword.value) {
    return sessions.value;
  }

  const keyword = searchKeyword.value.toLowerCase();
  return sessions.value.filter(
    (session) =>
      getSessionName(session).toLowerCase().includes(keyword) ||
      (session.lastMessage &&
        session.lastMessage.toLowerCase().includes(keyword))
  );
});

// 获取会话头像
const getSessionAvatar = (session) => {
  if (session.type === "company") {
    return (
      session.avatar ||
      "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"
    );
  } else if (session.type === "counselor") {
    return (
      session.avatar ||
      "https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png"
    );
  } else {
    return "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png";
  }
};

// 获取会话名称
const getSessionName = (session) => {
  return session.participantInfo?.name || session.name || session.title || getSessionTypeText(session.type);
};

// 获取会话类型显示文本
const getSessionTypeText = (type) => {
  const typeMap = {
    company: "企业",
    counselor: "辅导员",
    group: "群聊",
  };
  return typeMap[type] || "未知类型";
};

// 获取消息发送者头像
const getMessageAvatar = (message) => {
  if (message.senderType === "company") {
    return (
      message.senderAvatar ||
      "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"
    );
  } else if (message.senderType === "counselor") {
    return (
      message.senderAvatar ||
      "https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png"
    );
  } else {
    return (
      message.senderAvatar ||
      "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png"
    );
  }
};

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return "";

  const date = new Date(timestamp);
  const now = new Date();
  const diff = now - date;

  // 今天内的消息显示时分
  if (diff < 24 * 60 * 60 * 1000 && date.getDate() === now.getDate()) {
    return `${date.getHours().toString().padStart(2, "0")}:${date
      .getMinutes()
      .toString()
      .padStart(2, "0")}`;
  }

  // 昨天的消息显示"昨天"
  const yesterday = new Date(now);
  yesterday.setDate(yesterday.getDate() - 1);
  if (
    date.getDate() === yesterday.getDate() &&
    date.getMonth() === yesterday.getMonth() &&
    date.getFullYear() === yesterday.getFullYear()
  ) {
    return "昨天";
  }

  // 当年内的其他日期显示月日
  if (date.getFullYear() === now.getFullYear()) {
    return `${(date.getMonth() + 1).toString().padStart(2, "0")}-${date
      .getDate()
      .toString()
      .padStart(2, "0")}`;
  }

  // 其他年份的消息显示年月日
  return `${date.getFullYear()}-${(date.getMonth() + 1)
    .toString()
    .padStart(2, "0")}-${date.getDate().toString().padStart(2, "0")}`;
};

// 格式化完整时间
const formatFullTime = (timestamp) => {
  if (!timestamp) return "";

  const date = new Date(timestamp);
  const now = new Date();

  // 今天内的消息显示时分
  if (
    date.getDate() === now.getDate() &&
    date.getMonth() === now.getMonth() &&
    date.getFullYear() === now.getFullYear()
  ) {
    return `今天 ${date.getHours().toString().padStart(2, "0")}:${date
      .getMinutes()
      .toString()
      .padStart(2, "0")}`;
  }

  // 昨天的消息
  const yesterday = new Date(now);
  yesterday.setDate(yesterday.getDate() - 1);
  if (
    date.getDate() === yesterday.getDate() &&
    date.getMonth() === yesterday.getMonth() &&
    date.getFullYear() === yesterday.getFullYear()
  ) {
    return `昨天 ${date.getHours().toString().padStart(2, "0")}:${date
      .getMinutes()
      .toString()
      .padStart(2, "0")}`;
  }

  // 其他日期
  return `${date.getFullYear()}-${(date.getMonth() + 1)
    .toString()
    .padStart(2, "0")}-${date.getDate().toString().padStart(2, "0")} ${date
    .getHours()
    .toString()
    .padStart(2, "0")}:${date.getMinutes().toString().padStart(2, "0")}`;
};

// 判断是否显示消息时间
const showMessageTime = (message, index) => {
  if (index === 0) return true;

  const prevMessage = currentMessages.value[index - 1];
  const currentTime = new Date(message.sentAt).getTime();
  const prevTime = new Date(prevMessage.sentAt).getTime();

  // 如果两条消息间隔超过5分钟，显示时间
  return currentTime - prevTime > 5 * 60 * 1000;
};

// 加载会话列表 - 优化版本
const loadSessions = async (fromRetry = false) => {
  if (loadingSessions.value && !fromRetry) return; // 防止重复加载

  loadingSessions.value = true;
  console.log("开始加载会话列表，是否来自重试:", fromRetry);

  try {
    // 直接获取API原始响应
    const res = await getChatSessions({}).catch((err) => {
      console.error("会话API请求失败:", err);
      return null;
    });

    console.log("会话列表API响应:", res);

    if (res && typeof res === "object") {
      // 直接处理API原始响应格式
      if (res.error === 0 && res.body) {
        console.log("获取到会话列表:", res.body);
        sessions.value = Array.isArray(res.body.list) ? res.body.list : [];

        // 重置重试计数
        loadRetryCount.value = 0;

        // 确保DOM更新完成
        await nextTick();

        // 处理URL中的会话ID
        handleSessionFromUrl();

        // 显示明确成功消息
        if (fromRetry) {
          ElMessage.success("会话列表已刷新");
        }

        // 如果没有会话，提供提示
        if (sessions.value.length === 0) {
          console.log("没有发现会话");
        }

        return true;
      } else {
        console.error("API响应格式不符合预期:", res);
        throw new Error(res?.message || "加载会话列表失败");
      }
    } else {
      console.error("API响应无效:", res);
      throw new Error("获取会话列表失败，返回数据无效");
    }
  } catch (error) {
    console.error("加载会话列表失败:", error);
    ElMessage.error("加载会话列表失败，请刷新页面重试");
    handleLoadError();
    return false;
  } finally {
    // 无论成功失败，确保加载状态被重置
    loadingSessions.value = false;
  }
};

// 处理从URL加载会话逻辑
const handleSessionFromUrl = async () => {
  console.log("处理URL中的会话参数");
  // 如果有query参数，选择对应的会话
  const { sessionId } = route.query;
  if (sessionId) {
    console.log("URL中有sessionId参数:", sessionId);
    // 尝试将sessionId转换为数字
    const sessionIdNum = parseInt(sessionId, 10);
    if (!isNaN(sessionIdNum)) {
      console.log("寻找对应的会话，ID:", sessionIdNum);

      const session = sessions.value.find((s) => s.id === sessionIdNum);
      if (session) {
        console.log("找到对应会话，执行选择:", session);
        // 使用延迟确保DOM已渲染完成
        setTimeout(() => {
          selectSession(session);
        }, 100);
        return;
      } else {
        console.log("未找到对应的会话，尝试直接加载sessionId为", sessionIdNum);
        if (sessions.value.length > 0) {
          // 如果有会话但没找到指定ID，选择第一个会话
          console.log("选择第一个可用会话:", sessions.value[0]);
          setTimeout(() => {
            autoSelectFirstSession();
          }, 100);
          return;
        } else {
          // 没有会话但有sessionId - 创建一个临时会话
          console.log("创建临时会话");
          const dummySession = {
            id: sessionIdNum,
            type: "company",
            name: "加载中...",
            temporary: true,
          };
          currentSession.value = dummySession;
          setTimeout(() => {
            loadMessages();
          }, 100);
          return;
        }
      }
    }
  }

  // 如果没有URL参数但有会话，选择第一个
  if (sessions.value.length > 0) {
    console.log("没有sessionId参数，自动选择第一个会话");
    // 没有sessionId参数时，自动选择第一个会话
    autoSelectFirstSession();
  }
};

// 自动选择第一个会话
const autoSelectFirstSession = () => {
  if (sessions.value.length > 0) {
    console.log("自动选择第一个会话:", sessions.value[0]);
    nextTick(() => {
      selectSession(sessions.value[0]);
    });
  } else {
    // 没有会话可选择
    console.log("没有可选择的会话");
    ElMessage.warning("暂无可用会话");
  }
};

// 处理加载错误
const handleLoadError = () => {
  loadRetryCount.value++;

  if (loadRetryCount.value <= MAX_RETRIES) {
    console.log(`加载失败，${loadRetryCount.value}秒后自动重试...`);

    if (autoRetryTimer) {
      clearTimeout(autoRetryTimer);
    }

    autoRetryTimer = setTimeout(() => {
      loadSessions(true);
    }, loadRetryCount.value * 1000);
  } else {
    console.error("多次重试失败，请手动刷新");
    ElMessage.error("加载失败，请手动刷新页面");

    // 强制创建一个刷新按钮
    if (sessions.value.length === 0) {
      createEmergencyRefreshButton();
    }
  }
};

// 创建应急刷新按钮
const createEmergencyRefreshButton = () => {
  try {
    const container = document.querySelector(".chat-container");
    if (!container) return;

    // 检查是否已存在刷新按钮
    if (document.getElementById("emergency-refresh")) return;

    const button = document.createElement("button");
    button.id = "emergency-refresh";
    button.innerText = "点击刷新聊天";
    button.style.cssText =
      "position:fixed;top:50%;left:50%;transform:translate(-50%,-50%);" +
      "padding:10px 20px;background:#409eff;color:white;border:none;" +
      "border-radius:4px;font-size:16px;cursor:pointer;z-index:9999;";

    button.onclick = () => {
      window.location.reload();
    };

    container.appendChild(button);
    console.log("已创建应急刷新按钮");
  } catch (e) {
    console.error("创建应急按钮失败:", e);
  }
};

// 手动刷新会话列表
const refreshSessions = async () => {
  // 清除当前会话
  currentSession.value = null;
  currentMessages.value = [];
  chatError.value = false;

  // 重新加载会话
  await loadSessions(true);
};

// 选择会话 - 优化版
const selectSession = async (session) => {
  console.log("选择会话:", session);
  // 防止重复选择相同会话
  if (
    currentSession.value &&
    currentSession.value.id === session.id &&
    !session.temporary
  )
    return;

  try {
    currentSession.value = session;

    // 使用nextTick确保DOM更新后再操作
    await nextTick();

    // 重置消息相关状态
    currentMessages.value = [];
    hasMoreMessages.value = false;
    chatError.value = false;

    // 更新URL参数，但不触发新的导航
    if (route.query.sessionId !== String(session.id)) {
      router.replace({
        query: { ...route.query, sessionId: session.id },
      });
    }

    // 标记为已读
    if (session.unreadCount > 0) {
      try {
        await markSessionRead(session.id);
        session.unreadCount = 0;
      } catch (error) {
        console.error("标记消息已读失败:", error);
      }
    }

    // 在DOM更新后，延迟一点时间再加载消息
    setTimeout(() => {
      loadMessages();
    }, 100);
  } catch (error) {
    console.error("选择会话失败:", error);
    ElMessage.error("选择会话失败，请重试");
  }
};

// 加载消息记录 - 优化版
const loadMessages = async (isLoadMore = false) => {
  if (!currentSession.value) return;

  console.log(
    "开始加载消息，会话ID:",
    currentSession.value.id,
    "是否加载更多:",
    isLoadMore
  );

  const loadingInstance = ElLoading.service({
    target: messagesContainer.value,
    text: "加载消息中...",
  });

  try {
    // 构建查询参数
    const params = {
      sessionId: currentSession.value.id,
      page: isLoadMore ? 1 : 1,
      pageSize: 20,
      before:
        isLoadMore && currentMessages.value.length > 0
          ? currentMessages.value[0].id
          : undefined,
    };

    console.log("发送获取聊天消息请求，参数:", params);
    // 修正API调用 - 传递正确的参数格式并直接处理API原始响应
    const res = await getChatMessages(currentSession.value.id, {
      page: params.page,
      pageSize: params.pageSize,
      before: params.before,
    });
    console.log("获取聊天消息响应:", res);

    if (res && typeof res === "object" && res.error === 0 && res.body) {
      const messages = Array.isArray(res.body.list) ? res.body.list : [];
      console.log("获取到消息数量:", messages.length);

      // 修改会话临时标记
      if (currentSession.value.temporary) {
        currentSession.value.temporary = false;
      }

      if (isLoadMore) {
        // 在现有消息前添加更多消息
        currentMessages.value = [...messages, ...currentMessages.value];
      } else {
        // 完全替换消息列表
        currentMessages.value = messages;
      }

      hasMoreMessages.value = res.body.hasMore || false;

      // 如果是虚拟会话，尝试获取会话详细信息
      if (
        currentSession.value &&
        currentSession.value.name === "加载中..." &&
        messages.length > 0
      ) {
        // 从消息中获取会话信息
        const otherMsg = messages.find(
          (m) => m.senderId !== currentUserId.value
        );
        if (otherMsg) {
          currentSession.value.name = otherMsg.senderName || "未知用户";
          currentSession.value.type = otherMsg.senderType || "company";
          console.log("从消息中更新会话信息:", currentSession.value);
        }
      }

      // 重置错误状态
      chatError.value = false;
      errorMessage.value = "";
    } else {
      console.error("API响应格式不符合预期或有错误:", res);
      ElMessage.error(res?.message || "加载消息记录失败");
      chatError.value = true;
      errorMessage.value = res?.message || "加载消息记录失败";
    }

    loadingInstance.close();

    // 滚动到底部
    if (!isLoadMore) {
      nextTick(() => {
        scrollToBottom();
      });
    }
  } catch (error) {
    console.error("加载消息记录失败:", error);
    ElMessage.error("加载消息记录失败，请稍后重试");
    chatError.value = true;
    errorMessage.value = "加载消息记录失败，请稍后重试";
    loadingInstance.close();
  }
};

// 重试加载函数 - 优化版
const retryLoad = () => {
  chatError.value = false;
  errorMessage.value = "";

  console.log("重试加载聊天内容");
  if (currentSession.value) {
    // 如果会话存在，重新加载消息
    loadMessages();
  } else {
    // 如果会话不存在，重新加载会话列表
    loadSessions(true);
  }
};

// 生命周期钩子
onMounted(() => {
  console.log("Chat组件加载，检查URL参数");

  // 添加全局错误处理器
  window.addEventListener(
    "error",
    (e) => {
      // 拦截一切渲染错误
      if (
        e.message &&
        (e.message.includes("null") ||
          e.message.includes("undefined") ||
          e.message.includes("exmid"))
      ) {
        console.warn("已拦截渲染错误:", e.message);
        e.preventDefault();
        e.stopPropagation();
        return true;
      }
    },
    true
  );

  // 安装全局错误恢复机制
  installGlobalErrorRecovery();

  // 尝试修复已知的表情组件问题
  fixEmojiComponent();

  console.log("当前路由查询参数:", route.query);

  // 延迟加载，确保DOM完全渲染
  setTimeout(() => {
    loadSessions();
  }, 100);

  // 建立WebSocket连接
  connectWebSocket();
});

// 组件销毁前清理
onBeforeUnmount(() => {
  // 清理自动重试定时器
  if (autoRetryTimer) {
    clearTimeout(autoRetryTimer);
    autoRetryTimer = null;
  }

  // 清理全局错误处理
  window.removeEventListener("error", () => {});

  // 关闭WebSocket连接
  if (ws) {
    ws.close();
    ws = null;
  }
});

// DOM就绪后的延迟检查机制
setTimeout(() => {
  // 检测DOM是否已就绪且会话是否已加载
  if (sessions.value.length === 0) {
    console.log("延迟检测：会话列表为空，尝试恢复");
    // 会话加载可能失败但没有被正确处理
    loadSessions(true);
  }
}, 3000); // 3秒后检查

// 关闭会话信息侧栏
const handleCloseSessionInfo = () => {
  showSessionInfo.value = false;
};

// 删除会话
const deleteSession = async (sessionId) => {
  try {
    // 实际开发时使用API调用
    // await deleteSessionById(sessionId);

    // 模拟删除成功
    setTimeout(() => {
      const index = sessions.value.findIndex((s) => s.id === sessionId);
      if (index !== -1) {
        sessions.value.splice(index, 1);
      }

      if (currentSession.value && currentSession.value.id === sessionId) {
        currentSession.value = null;
        currentMessages.value = [];
      }

      showSessionInfo.value = false;
      ElMessage.success("会话已删除");
    }, 500);
  } catch (error) {
    console.error("删除会话失败:", error);
    ElMessage.error("删除会话失败，请稍后重试");
  }
};

// 举报会话
const reportSession = async (sessionId) => {
  try {
    // 实际开发时使用API调用
    // await reportSessionById(sessionId);

    // 模拟举报成功
    setTimeout(() => {
      showSessionInfo.value = false;
      ElMessage.success("举报已提交，我们会尽快处理");
    }, 500);
  } catch (error) {
    console.error("举报会话失败:", error);
    ElMessage.error("举报会话失败，请稍后重试");
  }
};

// 举报消息
const reportChatMsg = async (messageId, reason = "内容不当") => {
  try {
    const res = await reportChatMessage(messageId, reason);

    if (res && res.error === 0) {
      ElMessage.success("举报已提交，我们会尽快处理");
    } else {
      ElMessage.error(res?.message || "举报失败");
    }
  } catch (error) {
    console.error("举报消息失败:", error);
    ElMessage.error("举报失败，请稍后重试");
  }
};

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
  }
};

// 加载更多消息
const loadMoreMessages = () => {
  if (loadingMore.value || !hasMoreMessages.value) return;
  loadingMore.value = true;

  loadMessages(true).finally(() => {
    loadingMore.value = false;
  });
};

// 触发图片上传
const triggerImageUpload = () => {
  if (imageInput.value) {
    imageInput.value.click();
  }
};

// 处理图片上传
const handleImageUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  // 检查文件类型
  if (!file.type.startsWith("image/")) {
    ElMessage.error("请选择图片文件");
    return;
  }

  // 检查文件大小
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error("图片大小不能超过5MB");
    return;
  }

  try {
    const res = await uploadChatFile(currentSession.value.id, file, "image");
    console.log("上传图片响应:", res);

    if (res && typeof res === "object" && res.error === 0 && res.body) {
      // 发送图片消息
      const imageUrl = res.body.fileUrl;
      const messageData = {
        content: imageUrl,
        contentType: "image",
      };
      const msgRes = await sendChatMessage(
        currentSession.value.id,
        messageData
      );

      if (msgRes && typeof msgRes === "object" && msgRes.error === 0) {
        // 消息发送成功，刷新消息列表
        loadMessages();
      } else {
        console.error("发送图片消息失败:", msgRes);
        ElMessage.error(msgRes?.message || "发送图片消息失败");
      }
    } else {
      console.error("上传图片失败:", res);
      ElMessage.error(res?.message || "上传图片失败");
    }
  } catch (error) {
    console.error("上传图片失败:", error);
    ElMessage.error("上传图片失败，请稍后重试");
  } finally {
    // 清空文件输入框，方便下次选择相同文件
    if (imageInput.value) {
      imageInput.value.value = "";
    }
  }
};

// 触发文件上传
const triggerFileUpload = () => {
  if (fileInput.value) {
    fileInput.value.click();
  }
};

// 处理文件上传
const handleFileUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  // 检查文件大小
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error("文件大小不能超过10MB");
    return;
  }

  try {
    const res = await uploadChatFile(currentSession.value.id, file, "file");
    console.log("上传文件响应:", res);

    if (res && typeof res === "object" && res.error === 0 && res.body) {
      // 发送文件消息
      const fileData = res.body;
      const messageData = {
        content: fileData.fileUrl,
        contentType: "file",
        fileName: file.name,
      };
      const msgRes = await sendChatMessage(
        currentSession.value.id,
        messageData
      );

      if (msgRes && typeof msgRes === "object" && msgRes.error === 0) {
        // 消息发送成功，刷新消息列表
        loadMessages();
      } else {
        console.error("发送文件消息失败:", msgRes);
        ElMessage.error(msgRes?.message || "发送文件消息失败");
      }
    } else {
      console.error("上传文件失败:", res);
      ElMessage.error(res?.message || "上传文件失败");
    }
  } catch (error) {
    console.error("上传文件失败:", error);
    ElMessage.error("上传文件失败，请稍后重试");
  } finally {
    // 清空文件输入框，方便下次选择相同文件
    if (fileInput.value) {
      fileInput.value.value = "";
    }
  }
};

// WebSocket相关
let ws = null;
const wsConnected = ref(false);
const wsUrl = computed(() => {
  const token = userStore.token;
  // 自动适配后端端口，开发环境用8080，生产环境用当前域名
  let base = window.location.origin.replace(/^http/, 'ws');
  // 支持多端口开发环境，统一替换为8080
  base = base.replace(/:(5173|5174|5175)/, ':8080');
  return `${base}/ws/chat?token=${encodeURIComponent(token)}`;
});

// 建立WebSocket连接
const connectWebSocket = () => {
  if (ws) ws.close();
  ws = new WebSocket(wsUrl.value);
  ws.onopen = () => {
    wsConnected.value = true;
    console.log("WebSocket已连接");
  };
  ws.onclose = () => {
    wsConnected.value = false;
    console.log("WebSocket已断开");
    // 自动重连
    setTimeout(connectWebSocket, 3000);
  };
  ws.onerror = (e) => {
    wsConnected.value = false;
    console.error("WebSocket错误", e);
  };
  ws.onmessage = (event) => {
    try {
      console.log('收到WebSocket消息', event.data);
      const data = JSON.parse(event.data);
      if (data && data.type === 'chat_message' && data.sessionId && data.message) {
        // sessionId类型强制转为字符串对比，避免类型不一致
        const curSessionId = String(currentSession.value && currentSession.value.id);
        const msgSessionId = String(data.sessionId);
        if (currentSession.value && curSessionId === msgSessionId) {
          const msg = data.message;
          const isSelf = msg.senderId === currentUserId.value;
          let senderName = isSelf
            ? (userStore.userName || '我')
            : (currentSession.value?.name || '对方');
          let avatar = isSelf ? currentUserAvatar.value : (currentSession.value?.avatar || '');
          // 唯一性判断更健壮
          const msgId = msg.messageId || msg.id;
          if (!currentMessages.value.some(m => (m.messageId || m.id) === msgId)) {
            console.log('准备push到currentMessages', msg);
            currentMessages.value.push({
              ...msg,
              senderName,
              isSelf,
              avatar,
              status: 'sent',
            });
            console.log('currentMessages', currentMessages.value);
            nextTick(scrollToBottom);
          }
        }
        // 更新会话列表最新消息
        const idx = sessions.value.findIndex((s) => String(s.id) === msgSessionId);
        if (idx !== -1) {
          sessions.value[idx].lastMessage = data.message.content;
          sessions.value[idx].lastMessageTime = data.message.sentAt || Date.now();
          if (data.message.senderId !== currentUserId.value) {
            sessions.value[idx].unreadCount =
              (sessions.value[idx].unreadCount || 0) + 1;
          }
        }
      }
    } catch (e) {
      console.error("WebSocket消息解析失败", e);
    }
  };
};

// 发送文本消息（优先WebSocket）
const sendTextMessage = async () => {
  if (!messageInput.value.trim() || !currentSession.value) return;
  const content = messageInput.value;
  messageInput.value = "";
  // 不再本地插入临时消息，等待WebSocket推送
  if (ws && wsConnected.value) {
    try {
      ws.send(
        JSON.stringify({
          sessionId: currentSession.value.id,
          content,
          contentType: "text",
        })
      );
      // 等待WebSocket推送
      return;
    } catch (e) {
      console.error("WebSocket发送失败，降级API", e);
    }
  }
  // 降级API
  try {
    const messageData = { content, contentType: "text" };
    const res = await sendChatMessage(currentSession.value.id, messageData);
    // 不再本地插入消息，等待WebSocket推送
    if (!(res && typeof res === "object" && res.error === 0 && res.body)) {
      ElMessage.error(res?.message || "发送消息失败");
    }
  } catch (error) {
    ElMessage.error("发送消息失败，请稍后重试");
  }
};

const contactingCounselor = ref(false)

const contactCounselor = async () => {
  if (contactingCounselor.value) return
  contactingCounselor.value = true
  try {
    // 获取学生档案，拿到辅导员ID
    const res = await getStudentProfile()
    const counselorId = res?.body?.counselorId
    if (!counselorId) {
      ElMessage.error('未找到辅导员信息，无法发起会话')
      return
    }
    // 检查是否已有与辅导员的会话
    const exist = sessions.value.find(s => s.type === 'counselor' && s.participantId === counselorId)
    if (exist) {
      selectSession(exist)
      ElMessage.success('已切换到辅导员会话')
      return
    }
    // 创建新会话
    const createRes = await createStudentCounselorSession({ counselorId })
    if (createRes && createRes.error === 0 && createRes.body) {
      await loadSessions(true)
      // 自动切换到新会话
      const newSession = sessions.value.find(s => s.type === 'counselor' && s.participantId === counselorId)
      if (newSession) {
        selectSession(newSession)
      }
      ElMessage.success('已发起与辅导员的会话')
    } else {
      ElMessage.error(createRes?.message || '发起会话失败')
    }
  } catch (e) {
    ElMessage.error('联系辅导员失败')
  } finally {
    contactingCounselor.value = false
  }
}
</script>

<style scoped>
.chat-container {
  display: flex;
  height: calc(100vh - 120px);
  overflow: hidden;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.chat-sidebar {
  width: 300px;
  border-right: 1px solid #e6e6e6;
  display: flex;
  flex-direction: column;
}

.search-box {
  padding: 15px;
  border-bottom: 1px solid #e6e6e6;
}

.session-list {
  flex: 1;
  overflow-y: auto;
}

.session-item {
  padding: 12px 15px;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: background-color 0.3s;
}

.session-item:hover {
  background-color: #f5f7fa;
}

.session-item.active {
  background-color: #ecf5ff;
}

.session-badge {
  margin-right: 12px;
}

.session-info {
  flex: 1;
  min-width: 0;
}

.session-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.session-name {
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-time {
  font-size: 12px;
  color: #909399;
}

.session-message {
  font-size: 13px;
  color: #606266;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-header {
  height: 60px;
  padding: 0 20px;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-title {
  font-size: 16px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 10px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
  background-color: #f5f7fa;
}

.message-wrapper {
  margin-bottom: 15px;
}

.message-time {
  text-align: center;
  margin: 10px 0;
  font-size: 12px;
  color: #909399;
}

.message {
  display: flex;
  margin-bottom: 8px;
}

.message-self {
  justify-content: flex-end;
}

.message-other {
  justify-content: flex-start;
}

.message-avatar {
  margin: 0 8px;
}

.message-content {
  max-width: 70%;
  border-radius: 4px;
  overflow: hidden;
}

.message-text {
  padding: 10px 12px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.message-self .message-text {
  background-color: #ecf5ff;
}

.message-image {
  width: 200px;
  border-radius: 4px;
  overflow: hidden;
}

.message-file {
  padding: 10px 12px;
  background-color: #fff;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.loading-more {
  text-align: center;
  margin: 10px 0;
}

.chat-input {
  padding: 15px;
  border-top: 1px solid #e6e6e6;
}

.input-toolbar {
  margin-bottom: 10px;
}

.input-actions {
  text-align: right;
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
  gap: 15px;
}

.no-session {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-color: #f5f7fa;
}

.session-detail {
  padding: 20px;
}

.detail-header {
  text-align: center;
  margin-bottom: 30px;
}

.detail-item {
  margin-bottom: 15px;
}

.detail-label {
  font-weight: 500;
  margin-right: 10px;
}

.detail-actions {
  margin-top: 30px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.chat-error-container {
  text-align: center;
  padding: 30px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  background-color: #f8f8f8;
  border-radius: 8px;
  margin: 20px;
}

.retry-button {
  margin-top: 20px;
  width: 120px;
}

.empty-messages {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
  color: #909399;
}

.sessions-loading {
  padding: 20px;
}

.empty-sessions {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
}

.session-actions {
  padding: 10px;
  text-align: center;
  border-top: 1px solid #f0f0f0;
}

.refresh-button {
  margin: 10px auto;
}
</style>
