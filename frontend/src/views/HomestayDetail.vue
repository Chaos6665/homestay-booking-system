<template>
  <div class="page" v-if="detail">
    <el-row :gutter="20">
      <el-col :span="14">
        <el-card>
          <img :src="detail.coverImageUrl || defaultImg" class="cover" alt="cover"/>
          <h2 style="margin-top: 12px">{{ detail.title }}</h2>
          <p class="meta">{{ detail.city }} · {{ detail.address }}</p>
          <p class="desc">{{ detail.description }}</p>
        </el-card>

        <el-card class="detail-card" style="margin-top: 16px">
          <h3>民宿详情</h3>
          <p class="detail-text">{{ detailInfo.text }}</p>
          <div class="detail-photos" v-if="detailInfo.images && detailInfo.images.length">
            <img v-for="(src, idx) in detailInfo.images" :key="idx" :src="src" alt="detail"/>
          </div>
        </el-card>

        <el-card class="review-card" style="margin-top: 16px">
          <h3>用户评论</h3>
          <div v-if="reviews.length === 0" class="no-review">暂时还没有评论，快来抢第一条吧～</div>
          <div v-else>
            <div v-for="item in reviews" :key="item.id" class="review-item">
              <div class="review-header">
                <el-rate v-model="item.rating" disabled show-score score-template="{value} 分"/>
                <span class="time">{{ item.createdAt }}</span>
              </div>
              <div class="review-content">{{ item.content }}</div>
              <div v-if="item.merchantReply" class="merchant-reply">
                商家回复：{{ item.merchantReply }}
              </div>
            </div>
          </div>

          <div class="add-review" v-if="currentUser">
            <h4 style="margin-top: 16px">发表你的评论</h4>
            <el-form :model="reviewForm" label-width="80px">
              <el-form-item label="评分">
                <el-rate v-model="reviewForm.rating"/>
              </el-form-item>
              <el-form-item label="内容">
                <el-input
                    v-model="reviewForm.content"
                    type="textarea"
                    :rows="3"
                    maxlength="300"
                    show-word-limit
                    placeholder="说说你的真实感受吧"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="reviewLoading" @click="submitReview">提交评论</el-button>
              </el-form-item>
            </el-form>
          </div>
          <div v-else class="no-user">请先登录后再发表评论。</div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card>
          <h3>预约下单</h3>
          <el-form :model="form" label-width="80px">
            <el-form-item label="入住日期">
              <el-date-picker v-model="form.checkInDate" type="date" value-format="YYYY-MM-DD"/>
            </el-form-item>
            <el-form-item label="离店日期">
              <el-date-picker v-model="form.checkOutDate" type="date" value-format="YYYY-MM-DD"/>
            </el-form-item>
            <el-form-item label="人数">
              <el-input-number v-model="form.guestCount" :min="1" :max="detail.maxGuests || 10"/>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="loading" @click="createOrder">提交订单</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue';
import {useRoute} from 'vue-router';
import {ElMessage} from 'element-plus';
import request from '../api/request';

const route = useRoute();
const detail = ref(null);
const loading = ref(false);
const defaultImg =
    'https://img1.baidu.com/it/u=1907358127,2770897569&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=500';

const form = reactive({
  checkInDate: '',
  checkOutDate: '',
  guestCount: 1
});

const reviews = ref([]);
const reviewForm = reactive({
  rating: 0,
  content: ''
});
const reviewLoading = ref(false);
const currentUser = ref(null);

// 为不同民宿配置各自的详情文字和本地图片
// 键是民宿的 id，值包含 text 和 images 数组（图片路径从 public 目录开始写）
const detailExtras = {
  // 示例：id 为 1 的民宿
  1: {
    text: '上海外滩景观公寓位于上海的静安区，提供花园，距离南京东路步行街有1.9公里，距离上海火车站有2.9公里。\n\n' +
        '住宿提供免费WiFi，内部提供私人停车位。 房间配有空调、带用餐区的设施齐全的厨房、电视以及带淋浴设施、' +
        '免费洗浴用品和吹风机的私人浴室。  住宿还配备冰箱和电热水壶。 \n\n这家公寓距离人民广场有3公里，距离豫园有3.2公里。' +
        ' 上海虹桥国际机场距离住宿有18公里。',
    images: ['/images/detail/room1-1.jpg', '/images/detail/room1-2.jpg']
  },
  // 示例：id 为 2 的民宿
  2: {
    text: '秉承一房一设计的理念，颜值美宿If House 致力于为客户打造高颜值，高品质，高度个性化的住宿产品。\n\n' +
        '公寓位于广州市中心的黄金区域：珠江新城CBD商圈，毗邻美领馆。珠江新城／五羊邨双地铁交汇，花城广场、' +
        '高德置地广场、广东省博物馆等地标商场环绕周边，各种地道美食店如陶陶居，广州酒家，蚝德喜等。\n\n' +
        '所有房间单独采购装修，确保每间房子都不一样，让您感受不一样的居住体验。\n\n公寓致力于为客人提供高颜值，' +
        '高品质，高趣味的新型住宿空间，细心的服务、地道的行程规划，都会让您的旅途更加省心和放心。',
    images: ['/images/detail/room2-1.jpg', '/images/detail/room2-2.jpg']
  },
  3: {
    text: '花筑奢泰安利乐和喜苑坐落于山东的泰安，配备免费WiFi和免费私人停车位。\n\n客人可以使用热水浴池。' +

        '房间配有露台、空调、平板电视以及带淋浴设施和免费洗浴用品的私人浴室。所有房间均提供山景阳台。' +

        '这家家庭式住宿供应亚洲风味早餐。客人还可以在花园内放松身心。\n\n花筑奢泰安利乐和喜苑距离泰安方特欢乐世界有5.7公里，' +
        '距离泰安火车站有6.7公里。济南遥墙国际机场距离住宿有93公里',
    images: ['/images/detail/room3-1.jpg', '/images/detail/room3-2.jpg']
  },
  4: {
    text: '位于新安查的长白山西坡，距离佛库伦雪圈公园 14 公里，距离佛库伦湖 14 公里，距离长白山水乐园 16 公里。\n\n' +
        '该住宿点距离国松滑雪服务中心 16 公里，提供行李寄存服务。这家家庭式住宿配有平板电视。客人可以在附近体验滑雪和骑行。' +
        '住宿内部提供自行车租赁服务、滑雪通行证售票处和滑雪设备存放处。这家家庭式住宿距离长白山国际高尔夫球俱乐部松谷球会有20公里，' +
        '距离长白山景区西门有34公里。住宿距离长白山机场有17公里，提供付费机场接驳服务。',
    images: ['/images/detail/room4-1.jpg', '/images/detail/room4-2.jpg']
  },
  5: {
    text: '长白山保护开发区池北区半山温泉民宿客栈位于和平营子，享有山景，配有免费WiFi和免费私人停车位，距离长白山景区北门有2.8公里。' +
        '所有房间均配有空调和有线频道电视。部分房间提供带淋浴设施、免费洗浴用品、吹风机和拖鞋的私人浴室。这家家庭式住宿提供自助或亚洲风味早餐。' +
        '长白山保护开发区池北区半山温泉民宿客栈拥有一间供应中式美食的餐厅，可应要求提供普通素食。这家住宿距离长白山自然博物馆有28公里。' +
        '这家住宿距离长白山机场有105公里，提供免费班车服务',
    images: ['/images/detail/room5-1.jpg', '/images/detail/room5-2.jpg']
  },
  6: {
    text: '丽江夕露小榭客栈位于丽江古城中心，提供带中国传统家具和现代化设施的舒适住宿，还提供覆盖各处的免费WiFi。' +
        '客人可以免费使用自助洗衣和烘干设施。丽江夕露小榭客栈距离四方街和木府大厦有800米，距离大水车有1.3公里，' +
        '距离黑龙潭和丽江汽车客运站有1.5公里，距离丽江火车站有7公里，距离丽江三义机场23公里。客房装饰典雅，配有空调、' +
        '暖气、有线电视、沙发和电热水壶，还拥有带淋浴、吹风机和浴袍的私人浴室。',
    images: ['/images/detail/room6-1.jpg', '/images/detail/room6-2.jpg']
  },
  7: {
    text: '成都春熙路太古里怡程酒店位置很好，位于成都中心，配备带免费WiFi的空调客房、免费私人停车位和客房服务。这家酒店地理位置超棒，' +
        '坐落于锦江区，距离天府广场有14分钟步行路程，距离春熙路有500米。住宿为客人提供24小时前台、礼宾服务和旅游行程安排服务。' +
        '这家酒店的客房提供书桌、平板电视、私人浴室、床上用品和毛巾。成都春熙路太古里怡程酒店的部分房间享有市景。所有客房都拥有电热水壶。' +
        '所有客房均拥有衣柜。客人可以在这家住宿享用自助、美式或亚洲风味早餐。这家住宿拥有一间供应美式风味、阿根廷风味和中式美食的餐厅，' +
        '可应要求提供普通素食。成都春熙路太古里怡程酒店距离宽窄巷子有2.8公里，距离文殊院有3.1公里。成都双流国际机场距离住宿有17公里。',
    images: ['/images/detail/room7-1.jpg', '/images/detail/room7-2.jpg']
  },
  8: {
    text: '同里澜舍庭院民宿提供花园、共用休息室以及带庭院和免费WiFi的空调住宿，距离金鸡湖有26公里。' +
        '每间房间都提供带淋浴设施、免费洗浴用品、吹风机和拖鞋的私人浴室。这家家庭式住宿距离寒山寺有29公里，' +
        '距离留园有29公里。苏南硕放国际机场距离住宿有59公里。',
    images: ['/images/detail/room8-1.jpg', '/images/detail/room8-2.jpg']
  },
  9: {
    text: '北京一瓢客栈Beijing YiPiao Inn位于北京，提供免费WiFi、空调、露台和餐厅，距离颐和园有48公里，距离天安门广场有48公里。' +
        '每间房间都提供带淋浴设施、免费洗浴用品、吹风机和拖鞋的私人浴室。这家家庭式住宿的客人可以享用欧陆式早餐。' +
        '北京一瓢客栈Beijing YiPiao Inn距离大栅栏有48公里，距离前门大街有48公里。北京首都国际机场距离住宿有75公里。',
    images: ['/images/detail/room9-1.jpg', '/images/detail/room9-2.jpg']
  },
  10: {
    text: '上海星梦酒店位于上海，拥有季节性室外泳池、花园、露台和餐厅。这家住宿距离上海迪士尼乐园约有4.8公里，距离上海新国际博览中心有20公里，' +
        '距离上海环球金融中心有26公里。住宿配备客房服务以及免费WiFi。这家酒店的所有房间均配备电热水壶。客房提供带淋浴设施、' +
        '免费洗浴用品和吹风机的私人浴室。上海星梦酒店的所有客房均拥有空调和书桌。这家住宿供应亚洲风味早餐。' +
        '24小时前台工作人员会讲英语、西班牙语、法语和意大利语。上海星梦酒店距离金茂大厦有27公里，距离上海正大广场有27公里。' +
        '上海浦东国际机场距离住宿有10公里。',
    images: ['/images/detail/room10-1.jpg', '/images/detail/room10-2.jpg']
  },
  11: {
    text: '薰衣草之家（番禺万达/万博中心热门商圈）位于广州，距离长隆欢乐世界有2.3公里，距离广州南站有9.2公里，提供免费WiFi，享有市景。' +
        '部分房间提供带淋浴设施、免费洗浴用品、吹风机和拖鞋的私人浴室。' +
        '这家公寓距离琶洲站有16公里，距离广州电视观光塔有16公里。佛山沙堤机场距离住宿有41公里。',
    images: ['/images/detail/room11-1.jpg', '/images/detail/room11-2.jpg']
  },
  12: {
    text: '花筑奢泰安利乐和喜苑坐落于山东的泰安，配备免费WiFi和免费私人停车位。客人可以使用热水浴池。' +

        '房间配有露台、空调、平板电视以及带淋浴设施和免费洗浴用品的私人浴室。所有房间均提供山景阳台。' +

        '这家家庭式住宿供应亚洲风味早餐。客人还可以在花园内放松身心。花筑奢泰安利乐和喜苑距离泰安方特欢乐世界有5.7公里，' +
        '距离泰安火车站有6.7公里。济南遥墙国际机场距离住宿有93公里',
    images: ['/images/detail/room12-1.jpg', '/images/detail/room12-2.jpg']
  },
  13: {
    text: '枕窗听雨坐落于苏州，距离留园有3.3公里，距离中心8.3公里，配备免费WiFi、露台和空调住宿。' +
        '这家家庭式住宿提供带平板电视的休息区以及带免费洗浴用品、吹风机和淋浴设施的私人浴室。枕窗听雨提供自行车租赁服务。' +
        '这家住宿附近的热门地标包括玄妙观、苏州博物馆和北寺塔。苏南硕放国际机场距离住宿有39公里。',
    images: ['/images/detail/room13-1.jpg', '/images/detail/room13-2.jpg']
  },
  14: {
    text: '麦田天阅酒店位于成都，提供餐厅和免费WiFi。酒店距离铁路南站地铁站有15分钟步行路程。' +
        '酒店的每间客房均配有电视、空调、宽敞的办公区、iPhone便捷式扬声器、迷你吧、冰箱、带淋浴、' +
        '吹风机和浴袍的私人浴室以及卫星频道和有线频道。客人可从客房欣赏城市景致。麦田天阅酒店提供24小时前台、' +
        '酒吧、会议设施、共用休息室、游戏室和免费停车场。酒店距离春熙路购物区、锦里古街和武侯祠有15分钟车程，' +
        '距离成都双流国际机场有10分钟车程，距离宽窄巷有20分钟车程，距离铁路南站地铁站有5分钟步行路程。',
    images: ['/images/detail/room14-1.jpg', '/images/detail/room14-2.jpg']
  },
  15: {
    text: '北京吾己民宿-万里长城黄花城-水长城休闲民宿位于北京，享有山景，配备花园、共用休息室、露台、酒吧和烧烤设施。住宿提供免费WiFi。' +

        '每间房间都提供庭院、带冰箱的设施齐全的厨房、休息区、平板电视以及带淋浴设施和免费洗浴用品的私人浴室。' +

        '住宿还配备微波炉、迷你吧、炉灶、咖啡机和电热水壶。这家家庭式住宿的客人可以享用自助或欧陆式早餐。' +
        '如果客人想探索该地区，可以在周边地区体验徒步和垂钓。',
    images: ['/images/detail/room15-1.jpg', '/images/detail/room15-2.jpg']
  },
  16: {
    text: '丽江君泊轩微奢客栈坐落在丽江古城中心，距离四方街只有5分钟的步行路程，拥有纳西风格的传统建筑风格，其精心布置的客房提供免费WiFi。' +
        '丽江君泊轩微奢客栈距离四方街有10分钟的步行路程，距离丽江三义机场有40分钟车程，距离风景秀丽的玉泉公园约有2公里。' +
        '空调客房都配有典雅的神色木质陈设、宜人的中性色调和平板电视。独立浴室配有浴缸或热水淋浴。部分客房享有周围的绿色美景。' +
        '旅游咨询台为那些希望探索该地区的客人提供观光安排。24小时营业的前台还配有安全保险箱和行李储存处。',
    images: ['/images/detail/room16-1.jpg', '/images/detail/room16-2.jpg']
  },
  17: {
    text: '归墟花堂民宿配备餐厅、花园、共用休息室和酒吧，距离联峰山公园有4公里。住宿配备免费WiFi，内部提供私人停车位。' +
        '每间房间都提供带淋浴设施、免费洗浴用品、吹风机和拖鞋的私人浴室。' +
        '这家家庭式住宿配备阳光露台和烧烤设施供客人使用。归墟花堂民宿距离北戴河火车站有5.2公里，距离怪楼奇园有5.5公里。' +
        '住宿距离秦皇岛北戴河机场有50公里，提供付费机场接驳服务。',
    images: ['/images/detail/room17-1.jpg', '/images/detail/room17-2.jpg']
  }
};

const detailInfo = computed(() => {
  const id = detail.value?.id;
  if (id && detailExtras[id]) {
    return detailExtras[id];
  }
  return {
    text: detail.value?.description || '',
    images: [detail.value?.coverImageUrl || defaultImg]
  };
});

const loadDetail = async () => {
  const id = route.params.id;
  const res = await request.get(`/api/homestays/${id}`);
  if (res.code === 0) {
    detail.value = res.data;
  }
};

const loadReviews = async () => {
  const id = route.params.id;
  const res = await request.get(`/api/reviews/homestay/${id}`);
  if (res.code === 0) {
    reviews.value = res.data || [];
  }
};

const submitReview = async () => {
  if (!currentUser.value) {
    ElMessage.error('请先登录');
    return;
  }
  if (!reviewForm.rating || reviewForm.rating < 1) {
    ElMessage.error('请先给出评分');
    return;
  }
  if (!reviewForm.content) {
    ElMessage.error('请输入评论内容');
    return;
  }
  reviewLoading.value = true;
  try {
    const res = await request.post('/api/reviews', {
      homestayId: detail.value.id,
      userId: currentUser.value.id,
      rating: reviewForm.rating,
      content: reviewForm.content
    });
    if (res.code === 0) {
      ElMessage.success('评论成功');
      reviewForm.rating = 0;
      reviewForm.content = '';
      await loadReviews();
    } else {
      ElMessage.error(res.msg || '评论失败');
    }
  } catch (e) {
    ElMessage.error('请求失败');
  } finally {
    reviewLoading.value = false;
  }
};

const createOrder = async () => {
  if (!form.checkInDate || !form.checkOutDate) {
    ElMessage.error('请选择入住和离店日期');
    return;
  }
  const userJson = localStorage.getItem('user');
  if (!userJson) {
    ElMessage.error('请先登录');
    return;
  }
  const user = JSON.parse(userJson);
  loading.value = true;
  try {
    const res = await request.post('/api/orders', {
      userId: user.id,
      homestayId: detail.value.id,
      checkInDate: form.checkInDate,
      checkOutDate: form.checkOutDate,
      guestCount: form.guestCount
    });
    if (res.code === 0) {
      ElMessage.success('下单成功，订单状态：' + res.data.status);
    } else {
      ElMessage.error(res.msg || '下单失败');
    }
  } catch (e) {
    ElMessage.error('请求失败');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  const userJson = localStorage.getItem('user');
  if (userJson) {
    currentUser.value = JSON.parse(userJson);
  }
  loadDetail();
  loadReviews();
});
</script>

<style scoped>
.page {
  padding: 24px;
}

.cover {
  width: 100%;
  height: 450px;
  object-fit: cover;
  border-radius: 4px;
}

.meta {
  color: #888;
  margin-top: 4px;
}

.desc {
  margin-top: 12px;
  line-height: 1.6;
}

.detail-card {
  margin-top: 16px;
  padding-bottom: 16px;
}

.detail-text {
  margin-top: 8px;
  line-height: 1.6;
  color: #555;
  white-space: pre-line;
}

.detail-photos {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}

.detail-photos img {
  width: 100%;
  max-width: 520px;
  height: 220px;
  object-fit: cover;
  border-radius: 4px;
}

.review-card {
  margin-top: 16px;
}

.review-item {
  border-bottom: 1px solid #f0f0f0;
  padding: 12px 0;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.time {
  font-size: 12px;
  color: #999;
}

.review-content {
  margin-top: 4px;
}

.merchant-reply {
  margin-top: 6px;
  font-size: 13px;
  color: #409eff;
}

.no-review,
.no-user {
  color: #999;
  margin-top: 8px;
}

.add-review {
  margin-top: 12px;
}
</style>
