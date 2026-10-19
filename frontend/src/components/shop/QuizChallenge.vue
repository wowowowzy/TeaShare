<template>
  <div class="quiz-challenge">
    <!-- 题目详情弹窗 -->
    <el-dialog
      v-model="questionDialogVisible"
      title="趣味问答"
      width="500px"
      :close-on-click-modal="false"
      :show-close="false"
      center
      class="question-detail-dialog"
    >
      <div class="dialog-content">
        <div v-if="loading" class="dialog-loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>正在加载题目...</span>
        </div>
        <div v-else-if="errorMessage" class="dialog-error">
          <el-icon><WarningFilled /></el-icon>
          <span>{{ errorMessage }}</span>
          <el-button size="small" @click="loadQuestions" style="margin-top: 10px;">重新加载</el-button>
        </div>
        <template v-else>
          <div class="dialog-question-header">
            <span class="question-badge">共 {{ questions.length }} 题</span>
          </div>
          <div class="dialog-tip">
            <el-icon><InfoFilled /></el-icon>
            <span>答对3道以上可获得优惠券</span>
          </div>
        </template>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleExit" size="large">退出</el-button>
          <el-button type="primary" @click="startQuiz" size="large" :disabled="loading || questions.length === 0">
            开始答题
          </el-button>
        </div>
      </template>
    </el-dialog>

    <div class="quiz-header">
      <h2>趣味闯关</h2>
      <p class="quiz-tip">答对3道送优惠券哦</p>
    </div>

    <div class="quiz-container" v-if="questions.length > 0 && quizStarted">
      <div class="quiz-main">
        <div class="question-nav-sidebar">
          <div class="nav-title">题目导航</div>
          <div class="nav-items">
            <div
              v-for="(item, index) in questions"
              :key="index"
              class="nav-item"
              :class="{
                'active': currentIndex === index,
                'answered': userAnswers[index] !== null && userAnswers[index] !== undefined,
                'correct': showResult && correctAnswers[index] !== null && correctAnswers[index] !== undefined && correctAnswers[index] !== -1 && correctAnswers[index] === userAnswers[index],
                'wrong': showResult && correctAnswers[index] !== null && correctAnswers[index] !== undefined && correctAnswers[index] !== -1 && correctAnswers[index] !== userAnswers[index]
              }"
              @click="goToQuestion(index)"
            >
              {{ index + 1 }}
            </div>
          </div>
        </div>

        <div class="question-area">
          <div class="question-progress">
            <span>第 {{ currentIndex + 1 }} 题 / 共 {{ questions.length }} 题</span>
          </div>

          <div class="question-content" v-if="questions[currentIndex]">
            <h3 class="question-title">{{ questions[currentIndex].question }}</h3>

            <div class="options-list">
              <div
                v-for="(option, optIndex) in questions[currentIndex].options"
                :key="optIndex"
                class="option-item"
                :class="{
                  'selected': userAnswers[currentIndex] === optIndex,
                  'correct-option': showResult && correctAnswers[currentIndex] !== null && correctAnswers[currentIndex] !== undefined && correctAnswers[currentIndex] !== -1 && correctAnswers[currentIndex] === optIndex,
                  'wrong-option': showResult && userAnswers[currentIndex] === optIndex && (correctAnswers[currentIndex] === null || correctAnswers[currentIndex] === undefined || correctAnswers[currentIndex] === -1 || correctAnswers[currentIndex] !== optIndex)
                }"
                @click="selectOption(optIndex)"
              >
                <span class="option-letter">{{ String.fromCharCode(65 + optIndex) }}</span>
                <span class="option-text">{{ option }}</span>
                <span v-if="showResult && correctAnswers[currentIndex] !== null && correctAnswers[currentIndex] !== undefined && correctAnswers[currentIndex] !== -1 && correctAnswers[currentIndex] === optIndex" class="result-tag correct-tag">正确答案</span>
                <span v-else-if="showResult && userAnswers[currentIndex] === optIndex && (correctAnswers[currentIndex] === null || correctAnswers[currentIndex] === undefined || correctAnswers[currentIndex] === -1 || correctAnswers[currentIndex] !== optIndex)" class="result-tag wrong-tag">错误答案</span>
              </div>
            </div>

            <div v-if="showResult" class="current-question-analysis">
              <div class="analysis-section" v-if="correctAnswers[currentIndex] !== null && correctAnswers[currentIndex] !== undefined && correctAnswers[currentIndex] >= 0">
                <span class="analysis-label">正确答案：</span>
                <span class="correct-text">{{ questions[currentIndex].options[correctAnswers[currentIndex]] }}</span>
              </div>
              <div class="analysis-section analysis-loading" v-if="currentAnalyzingIndex !== -1 && currentAnalyzingIndex === currentIndex">
                <el-icon class="is-loading"><Loading /></el-icon>
                <span>正在获取AI解析...</span>
              </div>
              <div class="analysis-section" v-else-if="analysisResults[currentIndex]">
                <span class="analysis-label">解析：</span>
                <span>{{ analysisResults[currentIndex] }}</span>
              </div>
            </div>
          </div>
          <br>
          <div class="question-actions">
            <el-button @click="handleExit" :disabled="isSubmitting">退出</el-button>
            <el-button @click="prevQuestion" :disabled="currentIndex === 0 || isSubmitting || showResult">上一题</el-button>
            <el-button @click="nextQuestion" :disabled="currentIndex === questions.length - 1 || isSubmitting || showResult">下一题</el-button>
            <el-button type="primary" @click="submitQuiz" :disabled="!allAnswered || isSubmitting || showResult" v-if="!showResult">
              {{ isSubmitting ? '提交中...' : '提交答案' }}
            </el-button>
          </div>
        </div>
      </div>

      <div class="result-section" v-if="showResult">
        <div class="result-card">
          <h3>答题结果</h3>
          <div class="result-score">
            <span class="score-number">{{ correctCount }}</span>
            <span class="score-total">/ {{ questions.length }}</span>
          </div>
          <p class="result-message">{{ resultMessage }}</p>
        </div>
      </div>
    </div>

    <div v-else-if="!questionDialogVisible" class="quiz-loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>正在加载题目...</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { getQuizQuestions, submitQuizAnswers } from '../../api/quiz';
import { generateStream, createNewSession } from '../../api/ai';
import { ElMessage } from 'element-plus';
import { Loading } from '@element-plus/icons-vue';
import { InfoFilled } from '@element-plus/icons-vue';
import { WarningFilled } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';

const emit = defineEmits(['close']);
const router = useRouter();

const questions = ref([]);
const currentIndex = ref(0);
const userAnswers = ref([]);
const correctAnswers = ref([]);
const showResult = ref(false);
const resultMessage = ref('');
const analysisResults = ref([]);
const loading = ref(true);
const questionDialogVisible = ref(false);
const quizStarted = ref(false);
const errorMessage = ref('');
const isSubmitting = ref(false);
const currentAnalyzingIndex = ref(-1);

const currentQuestionData = computed(() => {
  if (questions.value.length === 0) return null;
  return {
    index: 0,
    question: questions.value[0]?.question || '',
    options: questions.value[0]?.options || []
  };
});

const startQuiz = () => {
  questionDialogVisible.value = false;
  quizStarted.value = true;
};

const allAnswered = computed(() => {
  return userAnswers.value.filter(a => a !== null && a !== undefined).length === questions.value.length;
});

const correctCount = computed(() => {
  let count = 0;
  userAnswers.value.forEach((answer, index) => {
    if (answer === null || answer === undefined) return;
    const correctAns = correctAnswers.value[index];
    if (correctAns === null || correctAns === undefined) return;
    if (answer === correctAns) {
      count++;
    }
  });
  return count;
});

const loadQuestions = async () => {
  loading.value = true;
  errorMessage.value = '';
  questionDialogVisible.value = true;
  try {
    const data = await getQuizQuestions();
    console.log('获取题目原始数据:', data);
    
    let rawQuestionList = [];
    
    if (data && Array.isArray(data)) {
      rawQuestionList = data;
    } else if (data && data.questions && Array.isArray(data.questions)) {
      rawQuestionList = data.questions;
    } else if (data && data.data && Array.isArray(data.data)) {
      rawQuestionList = data.data;
    }
    
    if (rawQuestionList.length === 0) {
      throw new Error('题目数据为空');
    }
    
    const questionList = rawQuestionList.map(item => ({
      question: item.questionContent || item.question || item.title || item.q || item.content || '',
      options: item.options || item.choices || item.answers || item.items || [],
      questionId: item.questionId
    }));
    
    const validQuestions = questionList.filter(q => q.question && q.options && q.options.length > 0);
    
    if (validQuestions.length === 0) {
      throw new Error('题目数据格式无效');
    }
    
    questions.value = validQuestions;
    userAnswers.value = new Array(validQuestions.length).fill(null);
    correctAnswers.value = new Array(validQuestions.length).fill(null);
    analysisResults.value = new Array(validQuestions.length).fill(null);
    
    console.log('处理后questions:', questions.value);
    console.log('初始化correctAnswers:', correctAnswers.value);
    console.log('初始化analysisResults:', analysisResults.value);
  } catch (error) {
    console.error('获取题目失败:', error);
    errorMessage.value = error.message || '无法加载题目，请稍后重试';
    questions.value = [];
    userAnswers.value = [];
    correctAnswers.value = [];
    ElMessage.error('题目加载失败: ' + (error.message || '请检查网络连接'));
  } finally {
    loading.value = false;
    console.log('题目加载完成, questions:', questions.value.length, 'loading:', loading.value);
  }
};

const selectOption = (optIndex) => {
  if (!showResult.value) {
    userAnswers.value[currentIndex.value] = optIndex;
  }
};

const goToQuestion = (index) => {
  currentIndex.value = index;
};

const prevQuestion = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--;
  }
};

const nextQuestion = () => {
  if (currentIndex.value < questions.value.length - 1) {
    currentIndex.value++;
  }
};

const handleExit = () => {
  emit('close');
  router.push('/shop');
};

const submitQuiz = async () => {
  if (isSubmitting.value) return;
  
  const questionIds = questions.value.map(q => q.questionId);
  const optionLabels = userAnswers.value.map(answerIndex => {
    if (answerIndex === null || answerIndex === undefined) return '';
    return String.fromCharCode(65 + answerIndex);
  });
  
  console.log('提交答案 - questionIds:', questionIds);
  console.log('提交答案 - optionLabels:', optionLabels);
  
  isSubmitting.value = true;
  
  try {
    const res = await submitQuizAnswers(questionIds, optionLabels);
    console.log('提交答案响应:', res);
    console.log('响应类型:', typeof res);
    if (res) {
      console.log('响应keys:', Object.keys(res));
    }
    
    let correctAnswerList = [];
    
    if (Array.isArray(res)) {
      console.log('响应是数组格式');
      correctAnswerList = res.map(ans => {
        if (typeof ans === 'string') {
          const charCode = ans.toUpperCase().charCodeAt(0);
          if (charCode >= 65 && charCode <= 90) {
            console.log('字符串答案转换为索引:', ans, '->', charCode - 65);
            return charCode - 65;
          }
          const parsed = parseInt(ans);
          if (!isNaN(parsed)) return parsed;
          return 0;
        }
        return ans;
      });
    } else if (res && typeof res === 'object') {
      let answerData = null;
      
      if (res.answerList && Array.isArray(res.answerList)) {
        answerData = res.answerList;
        console.log('找到答案字段: answerList, 值:', answerData);
        
        correctAnswerList = answerData.map(item => {
          const optionLabel = item.optionLabel;
          if (typeof optionLabel === 'string') {
            const charCode = optionLabel.toUpperCase().charCodeAt(0);
            if (charCode >= 65 && charCode <= 90) {
              console.log('答案转换为索引:', optionLabel, '->', charCode - 65);
              return charCode - 65;
            }
            const parsed = parseInt(optionLabel);
            if (!isNaN(parsed)) return parsed;
            return 0;
          }
          return optionLabel;
        });
      } else {
        const possibleAnswerFields = [
          'correctAnswers', 'answers', 'answerList',
          'correctAnswer', 'answer', 'results', 'result', 'data'
        ];
        
        for (const field of possibleAnswerFields) {
          if (res[field] !== undefined && res[field] !== null) {
            answerData = res[field];
            console.log('找到答案字段:', field, '值:', answerData);
            break;
          }
        }
        
        if (answerData && Array.isArray(answerData)) {
          correctAnswerList = answerData.map(ans => {
            if (typeof ans === 'string') {
              const charCode = ans.toUpperCase().charCodeAt(0);
              if (charCode >= 65 && charCode <= 90) {
                console.log('字符串答案转换为索引:', ans, '->', charCode - 65);
                return charCode - 65;
              }
              const parsed = parseInt(ans);
              if (!isNaN(parsed)) return parsed;
              return 0;
            }
            return ans;
          });
        } else if (answerData && typeof answerData === 'object') {
          console.log('答案数据是对象:', answerData);
          const entries = Object.entries(answerData);
          if (entries.length > 0) {
            correctAnswerList = entries.map(([key, val]) => {
              if (typeof val === 'string') {
                const charCode = val.toUpperCase().charCodeAt(0);
                if (charCode >= 65 && charCode <= 90) {
                  return charCode - 65;
                }
                const parsed = parseInt(val);
                if (!isNaN(parsed)) return parsed;
                return 0;
              }
              return val;
            });
          }
        }
      }
    }
    
    if (correctAnswerList.length === 0 && questions.value.length > 0) {
      console.warn('未能从响应中解析出正确答案，答案为空');
      correctAnswerList = new Array(questions.value.length).fill(null);
    }
    
    console.log('解析后的正确答案索引:', correctAnswerList);
    correctAnswers.value = correctAnswerList;
    console.log('更新后的correctAnswers:', correctAnswers.value);
    showResult.value = true;
    
    if (res && res.msg) {
      resultMessage.value = res.msg;
    } else if (res && res.message) {
      resultMessage.value = res.message;
    } else if (res && res.data && res.data.msg) {
      resultMessage.value = res.data.msg;
    }
    
    ElMessage.success(resultMessage.value || '提交成功');
    
    currentIndex.value = 0;
    
    for (let i = 0; i < questions.value.length; i++) {
      currentAnalyzingIndex.value = i;
      await getAnalysis(i);
    }
  } catch (error) {
    console.error('提交答案失败:', error);
    ElMessage.error('提交失败: ' + (error.message || '请稍后重试'));
    
    showResult.value = true;
    resultMessage.value = '答题完成';
    currentIndex.value = 0;
    
    for (let i = 0; i < questions.value.length; i++) {
      currentAnalyzingIndex.value = i;
      await getAnalysis(i);
    }
  } finally {
    isSubmitting.value = false;
    currentAnalyzingIndex.value = -1;
  }
};

const getAnalysis = async (index) => {
  const question = questions.value[index];
  const correctIndex = correctAnswers.value[index];
  
  if (correctIndex === null || correctIndex === undefined) {
    const newResults = [...analysisResults.value];
    newResults[index] = '暂无解析';
    analysisResults.value = newResults;
    return;
  }
  
  const correctOption = question.options[correctIndex];
  const optionLabel = String.fromCharCode(65 + correctIndex);

  const prompt = `请简要解析这道茶文化题目：
题目：${question.question}
正确答案：${optionLabel}. ${correctOption}
请用50字以内的中文给出解析。`;

  try {
    // 1. 先获取新的sessionId
    console.log('开始获取新sessionId...');
    const sessionResponse = await createNewSession();
    console.log('获取sessionId响应:', sessionResponse);
    
    // 由于request.js拦截器处理，返回的就是data部分，即sessionId
    let sessionId = sessionResponse;
    if (sessionId) {
      sessionId = String(sessionId);
      // 存储到localStorage
      localStorage.setItem('sessionId', sessionId);
      console.log('获取新sessionId成功:', sessionId);
    } else {
      console.error('未能获取到sessionId');
    }
    
    // 2. 使用新sessionId调用AI接口生成解析（流式处理）
    console.log('请求AI解析, prompt:', prompt, ', sessionId:', sessionId);
    
    // 初始化解析结果
    const newResults = [...analysisResults.value];
    newResults[index] = '';
    analysisResults.value = newResults;
    
    // 使用流式处理
    await generateStream(
      prompt, 
      sessionId, 
      null,
      (chunk) => {
        // 处理接收到的流式数据（实时）
        console.log('接收到解析数据:', chunk);
        
        // 实时更新解析结果
        const updatedResults = [...analysisResults.value];
        updatedResults[index] += chunk;
        analysisResults.value = updatedResults;
      },
      () => {
        // 流式响应完成
        console.log('解析流式响应完成');
      },
      (error) => {
        // 流式响应错误
        console.error('解析流式响应错误:', error);
        const errorResults = [...analysisResults.value];
        errorResults[index] = '暂无解析';
        analysisResults.value = errorResults;
      }
    );
    
    // 确保解析结果不为空
    const finalResults = [...analysisResults.value];
    if (!finalResults[index] || finalResults[index].trim() === '') {
      finalResults[index] = '暂无解析';
      analysisResults.value = finalResults;
    }
    
    console.log('更新analysisResults:', index, analysisResults.value[index]);
  } catch (error) {
    console.error('获取解析失败:', error);
    const newResults = [...analysisResults.value];
    newResults[index] = '暂无解析';
    analysisResults.value = newResults;
  }
};

onMounted(() => {
  loadQuestions();
});
</script>

<style scoped>
.quiz-challenge {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7);
  z-index: 1000;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.quiz-header {
  display: none;
}

.quiz-container {
  background-color: #fff;
  border-radius: 12px;
  width: 95%;
  max-width: 1200px;
  max-height: 90vh;
  overflow-y: auto;
  padding: 30px;
  position: relative;
}

.quiz-main {
  display: flex;
  gap: 30px;
  align-items: center;
}

.question-nav-sidebar {
  flex-shrink: 0;
  width: 100px;
  background-color: #f5f5f5;
  border-radius: 8px;
  padding: 20px 15px;
  height: fit-content;
}

.nav-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 15px;
  text-align: center;
}

.nav-items {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.nav-item {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fff;
  border: 2px solid #ddd;
  border-radius: 8px;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.3s;
}

.nav-item.active {
  border-color: #4ba88f;
  background-color: #4ba88f;
  color: #fff;
}

.nav-item.answered {
  border-color: #409eff;
  /* color: #409eff; */
}

.nav-item.correct {
  border-color: #67c23a;
  background-color: #67c23a;
  color: #fff;
}

.nav-item.wrong {
  border-color: #f56c6c;
  background-color: #f56c6c;
  color: #fff;
}

.question-area {
  flex: 1;
}

.question-progress {
  margin-bottom: 20px;
  color: #666;
  font-size: 14px;
}

.question-title {
  font-size: 20px;
  color: #333;
  margin-bottom: 30px;
  line-height: 1.6;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 30px;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  background-color: #f5f5f5;
  border: 2px solid #ddd;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.option-item:hover {
  border-color: #4ba88f;
  background-color: #f0f9f6;
}

.option-item.selected {
  border-color: #67c23a;
  background-color: #e1f3d8;
}

.option-item.correct-option {
  border-color: #67c23a;
  background-color: #e1f3d8;
}

.option-item.wrong-option {
  border-color: #f56c6c;
  background-color: #fde2e2;
}

.option-letter {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #4ba88f;
  color: #fff;
  border-radius: 50%;
  font-weight: bold;
  margin-right: 15px;
  flex-shrink: 0;
}

.option-item.selected .option-letter {
  /* background-color: #fff; */
}

.option-item.correct-option .option-letter {
  background-color: #67c23a;
}

.option-item.wrong-option .option-letter {
  background-color: #f56c6c;
}

.option-text {
  font-size: 16px;
  flex: 1;
}

.result-tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  margin-left: 10px;
  flex-shrink: 0;
}

.correct-tag {
  background-color: #67c23a;
  color: #fff;
}

.wrong-tag {
  background-color: #f56c6c;
  color: #fff;
}

.current-question-analysis {
  margin-top: 20px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.analysis-section {
  margin-bottom: 10px;
  display: flex;
  align-items: flex-start;
}

.analysis-section:last-child {
  margin-bottom: 0;
}

.analysis-label {
  font-weight: bold;
  color: #4ba88f;
  margin-right: 8px;
  flex-shrink: 0;
}

.analysis-loading {
  color: #999;
  display: flex;
  align-items: center;
  gap: 8px;
}

.question-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
}

.result-section {
  margin-top: 40px;
  padding-top: 30px;
  border-top: 2px solid #eee;
}

.result-card {
  background: linear-gradient(135deg, #4ba88f 0%, #2c6e5f 100%);
  color: #fff;
  padding: 30px;
  border-radius: 12px;
  text-align: center;
  margin-bottom: 30px;
}

.result-card h3 {
  font-size: 24px;
  margin-bottom: 20px;
}

.result-score {
  font-size: 48px;
  margin-bottom: 15px;
}

.score-number {
  font-weight: bold;
}

.result-message {
  font-size: 18px;
  opacity: 0.9;
}

.answers-analysis h3 {
  font-size: 20px;
  color: #333;
  margin-bottom: 20px;
}

.analysis-item {
  background-color: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.analysis-question {
  margin-bottom: 15px;
}

.analysis-num {
  font-weight: bold;
  color: #4ba88f;
  margin-right: 8px;
}

.analysis-answer {
  margin-bottom: 10px;
  color: #666;
}

.correct-text {
  color: #67c23a;
  font-weight: bold;
}

.analysis-explanation {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
}

.analysis-loading {
  color: #999;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.quiz-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #666;
  gap: 15px;
  padding: 50px;
}

.quiz-loading .el-icon {
  font-size: 32px;
}

.question-detail-dialog .el-dialog {
  border-radius: 16px;
  overflow: hidden;
}

.question-detail-dialog .el-dialog__header {
  background: linear-gradient(135deg, #4ba88f 0%, #2c6e5f 100%);
  padding: 20px;
  margin: 0;
}

.question-detail-dialog .el-dialog__title {
  color: #fff;
  font-size: 20px;
  font-weight: bold;
}

.question-detail-dialog .el-dialog__body {
  padding: 30px;
}

.dialog-content {
  text-align: center;
}

.dialog-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #666;
  gap: 15px;
}

.dialog-loading .el-icon {
  font-size: 32px;
  color: #4ba88f;
}

.dialog-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #f56c6c;
  gap: 15px;
}

.dialog-error .el-icon {
  font-size: 32px;
}

.dialog-question-header {
  margin-bottom: 20px;
}

.question-badge {
  display: inline-block;
  background: linear-gradient(135deg, #4ba88f 0%, #2c6e5f 100%);
  color: #fff;
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: bold;
}

.dialog-question-text {
  margin-bottom: 30px;
}

.dialog-question-text h3 {
  font-size: 22px;
  color: #333;
  line-height: 1.6;
  margin: 0;
}

.dialog-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 25px;
}

.dialog-option-item {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  background-color: #f5f5f5;
  border-radius: 8px;
  text-align: left;
}

.option-label {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #4ba88f;
  color: #fff;
  border-radius: 50%;
  font-weight: bold;
  margin-right: 15px;
  flex-shrink: 0;
}

.option-content {
  font-size: 16px;
  color: #333;
}

.dialog-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #999;
  font-size: 14px;
  padding: 12px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.dialog-tip .el-icon {
  color: #4ba88f;
}

.dialog-footer {
  text-align: center;
}

.dialog-footer .el-button {
  padding: 12px 40px;
  font-size: 16px;
}

@media (max-width: 768px) {
  .quiz-main {
    flex-direction: column;
  }

  .question-nav-sidebar {
    width: 100%;
  }

  .nav-items {
    grid-template-columns: repeat(5, 1fr);
  }

  .question-detail-dialog .el-dialog {
    width: 95% !important;
    margin: 10px auto !important;
  }

  .question-detail-dialog .el-dialog__body {
    padding: 20px 15px;
  }

  .dialog-question-text h3 {
    font-size: 18px;
  }

  .dialog-option-item {
    padding: 12px 15px;
  }

  .option-label {
    width: 28px;
    height: 28px;
    margin-right: 10px;
  }

  .option-content {
    font-size: 14px;
  }

  .dialog-footer .el-button {
    padding: 10px 30px;
    font-size: 14px;
  }
}
</style>
