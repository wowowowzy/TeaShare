<template>
  <el-dialog
    :model-value="modelValue"
    :title="editing ? '编辑地址' : '添加地址'"
    width="90%"
    style="max-width: 520px"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <form class="space-y-6" novalidate @submit.prevent="submit">
      <div v-for="f in fields" :key="f.key">
        <label :for="`addr-${f.key}`" class="eyebrow mb-2 block">{{ f.label }}</label>
        <input
          :id="`addr-${f.key}`"
          v-model.trim="form[f.key]"
          type="text"
          class="field-input"
          :placeholder="f.placeholder || ''"
          @blur="validate(f.key)"
        />
        <p v-if="errors[f.key]" class="mt-2 font-sans text-xs text-[#1C1C1C]/60">
          {{ errors[f.key] }}
        </p>
      </div>

      <label class="flex cursor-pointer items-center gap-3">
        <input v-model="form.isDefault" type="checkbox" class="accent-[#1C1C1C]" />
        <span class="font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/60 uppercase">
          设为默认地址
        </span>
      </label>
    </form>

    <template #footer>
      <div class="flex justify-end gap-3">
        <button type="button" class="btn-editorial-ghost !px-4 !py-2 text-xs" @click="close">
          取消
        </button>
        <button type="button" class="btn-editorial !px-4 !py-2 text-xs" :disabled="saving" @click="submit">
          {{ saving ? '保存中…' : '保存' }}
        </button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  /** 传入表示编辑，null 表示新增 */
  address: { type: Object, default: null }
})
const emit = defineEmits(['update:modelValue', 'saved'])

const fields = [
  { key: 'name', label: '收件人姓名', placeholder: '请输入收件人姓名' },
  { key: 'phone', label: '手机号', placeholder: '请输入 11 位手机号' },
  { key: 'province', label: '省份', placeholder: '如：广东省' },
  { key: 'city', label: '城市', placeholder: '如：广州市' },
  { key: 'district', label: '区县', placeholder: '如：天河区' },
  { key: 'detail', label: '详细地址', placeholder: '街道、门牌号等' },
  { key: 'postalCode', label: '邮编（可选）', placeholder: '6 位邮编' }
]

const emptyForm = () => ({
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  postalCode: '',
  isDefault: false
})

const form = reactive(emptyForm())
const errors = reactive({})
const saving = ref(false)

const editing = ref(false)

watch(
  () => props.modelValue,
  (open) => {
    if (!open) return
    Object.assign(form, emptyForm())
    Object.keys(errors).forEach((k) => delete errors[k])
    if (props.address) {
      editing.value = true
      Object.assign(form, {
        name: props.address.name || '',
        phone: props.address.phone || '',
        province: props.address.province || '',
        city: props.address.city || '',
        district: props.address.district || '',
        detail: props.address.detail || '',
        postalCode: props.address.postalCode || '',
        isDefault: props.address.isDefault === 1
      })
    } else {
      editing.value = false
    }
  }
)

function validate(key) {
  const v = form[key]
  switch (key) {
    case 'name':
      errors.name = !v ? '请输入收件人姓名' : v.length < 2 || v.length > 20 ? '姓名长度 2-20 个字符' : ''
      break
    case 'phone':
      errors.phone = !v ? '请输入手机号' : /^1[3-9]\d{9}$/.test(v) ? '' : '请输入正确的手机号'
      break
    case 'province':
      errors.province = !v ? '请输入省份' : ''
      break
    case 'city':
      errors.city = !v ? '请输入城市' : ''
      break
    case 'district':
      errors.district = !v ? '请输入区县' : ''
      break
    case 'detail':
      errors.detail = !v ? '请输入详细地址' : v.length < 5 || v.length > 200 ? '详细地址 5-200 个字符' : ''
      break
    case 'postalCode':
      errors.postalCode = !v || /^\d{6}$/.test(v) ? '' : '请输入 6 位邮编'
      break
  }
  return !errors[key]
}

function validateAll() {
  return ['name', 'phone', 'province', 'city', 'district', 'detail', 'postalCode']
    .map(validate)
    .every(Boolean)
}

function close() {
  emit('update:modelValue', false)
}

function submit() {
  if (!validateAll()) return
  emit('saved', {
    ...form,
    isDefault: form.isDefault ? 1 : 0,
    ...(editing.value && props.address?.id ? { id: props.address.id } : {})
  })
  saving.value = false
}

defineExpose({ setSaving: (v) => (saving.value = v) })
</script>
