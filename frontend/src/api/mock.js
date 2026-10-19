// 模拟API接口
const mockApi = {
  // 用户登录
  login: (username, password) => {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          code: 200,
          data: {
            token: 'mock-token',
            user: {
              id: 1,
              name: username,
              email: `${username}@example.com`
            }
          },
          message: '登录成功'
        });
      }, 500);
    });
  },

  // 获取用户信息
  getUserInfo: () => {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          code: 200,
          data: {
            id: 1,
            name: '张三',
            email: 'zhangsan@example.com',
            phone: '13800138000'
          },
          message: '获取成功'
        });
      }, 500);
    });
  },

  // 获取茶种列表
  getTeaVarieties: () => {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          code: 200,
          data: [
            {
              id: 1,
              name: "凤凰单丛",
              origin: "广东潮州",
              aroma: "花香、蜜香",
              characteristics: "香气高锐，滋味醇厚"
            },
            {
              id: 2,
              name: "英德红茶",
              origin: "广东英德",
              aroma: "甜香、花香",
              characteristics: "滋味醇厚，汤色红亮"
            },
            {
              id: 3,
              name: "岭头单丛",
              origin: "广东饶平",
              aroma: "花香、果香",
              characteristics: "香气浓郁，滋味甘醇"
            },
            {
              id: 4,
              name: "凤凰水仙",
              origin: "广东潮州",
              aroma: "兰花香",
              characteristics: "滋味醇厚，回甘持久"
            }
          ],
          message: '获取成功'
        });
      }, 500);
    });
  },

  // 获取茶器列表
  getTeaWare: () => {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          code: 200,
          data: [
            {
              id: 1,
              name: "潮州手拉朱泥壶",
              description: "潮州手拉朱泥壶是广东潮州的传统工艺，以其精湛的手工技艺和独特的造型而闻名。",
              image: "https://picsum.photos/800/800?random=23"
            },
            {
              id: 2,
              name: "工夫茶四宝",
              description: "工夫茶四宝包括茶壶、茶杯、茶盘和茶炉，是广东工夫茶的必备器具。",
              image: "https://picsum.photos/800/800?random=24"
            },
            {
              id: 3,
              name: "潮汕工夫茶杯",
              description: "潮汕工夫茶杯小巧玲珑，便于品饮，是工夫茶品饮的重要器具。",
              image: "https://picsum.photos/800/800?random=25"
            },
            {
              id: 4,
              name: "茶盘",
              description: "茶盘用于放置茶具，承接茶渣和废水，是工夫茶品饮的重要组成部分。",
              image: "https://picsum.photos/800/800?random=26"
            }
          ],
          message: '获取成功'
        });
      }, 500);
    });
  },

  // 获取商品列表
  getProducts: () => {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          code: 200,
          data: [
            {
              id: 1,
              name: "凤凰单丛茶",
              price: 298,
              description: "广东潮州特产，香气高锐，滋味醇厚",
              image: "https://picsum.photos/800/800?random=27"
            },
            {
              id: 2,
              name: "英德红茶",
              price: 198,
              description: "广东英德特产，滋味醇厚，汤色红亮",
              image: "https://picsum.photos/800/800?random=28"
            },
            {
              id: 3,
              name: "潮州手拉朱泥壶",
              price: 598,
              description: "广东潮州传统工艺，手工制作，造型精美",
              image: "https://picsum.photos/800/800?random=29"
            },
            {
              id: 4,
              name: "工夫茶四宝",
              price: 398,
              description: "广东工夫茶必备器具，包含茶壶、茶杯、茶盘、茶炉",
              image: "https://picsum.photos/800/800?random=30"
            },
            {
              id: 5,
              name: "岭头单丛茶",
              price: 258,
              description: "广东饶平特产，香气浓郁，滋味甘醇",
              image: "https://picsum.photos/800/800?random=31"
            },
            {
              id: 6,
              name: "凤凰水仙茶",
              price: 238,
              description: "广东潮州特产，兰花香，滋味醇厚",
              image: "https://picsum.photos/800/800?random=32"
            }
          ],
          message: '获取成功'
        });
      }, 500);
    });
  },

  // 获取商品详情
  getProductDetail: (id) => {
    return new Promise((resolve) => {
      setTimeout(() => {
        const products = [
          {
            id: 1,
            name: "凤凰单丛茶",
            price: 298,
            description: "广东潮州特产，香气高锐，滋味醇厚。凤凰单丛茶是潮州凤凰山特产，属于乌龙茶类，因成品茶香气各异，故称为单丛。",
            image: "https://picsum.photos/800/800?random=33",
            stock: 100
          },
          {
            id: 2,
            name: "英德红茶",
            price: 198,
            description: "广东英德特产，滋味醇厚，汤色红亮。英德红茶是中国著名的红茶品种之一，以其鲜爽的口感和浓郁的香气而闻名。",
            image: "https://picsum.photos/800/800?random=34",
            stock: 150
          },
          {
            id: 3,
            name: "潮州手拉朱泥壶",
            price: 598,
            description: "广东潮州传统工艺，手工制作，造型精美。潮州手拉朱泥壶是潮州特有的传统工艺，以其独特的制作工艺和精美的造型而闻名。",
            image: "https://picsum.photos/800/800?random=35",
            stock: 50
          },
          {
            id: 4,
            name: "工夫茶四宝",
            price: 398,
            description: "广东工夫茶必备器具，包含茶壶、茶杯、茶盘、茶炉。工夫茶四宝是广东工夫茶品饮的必备器具，体现了广东茶文化的精髓。",
            image: "https://picsum.photos/800/800?random=36",
            stock: 80
          },
          {
            id: 5,
            name: "岭头单丛茶",
            price: 258,
            description: "广东饶平特产，香气浓郁，滋味甘醇。岭头单丛茶是饶平岭头特产，属于乌龙茶类，以其浓郁的香气和甘醇的滋味而闻名。",
            image: "https://picsum.photos/800/800?random=37",
            stock: 120
          },
          {
            id: 6,
            name: "凤凰水仙茶",
            price: 238,
            description: "广东潮州特产，兰花香，滋味醇厚。凤凰水仙茶是潮州凤凰山特产，属于乌龙茶类，以其独特的兰花香和醇厚的滋味而闻名。",
            image: "https://picsum.photos/800/800?random=38",
            stock: 90
          }
        ];
        const product = products.find(p => p.id === parseInt(id));
        resolve({
          code: 200,
          data: product,
          message: '获取成功'
        });
      }, 500);
    });
  },

  // 获取订单列表
  getOrders: () => {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          code: 200,
          data: [
            {
              id: "20230601001",
              status: "已完成",
              items: [
                { name: "凤凰单丛茶", price: 298, quantity: 1 },
                { name: "潮州手拉朱泥壶", price: 598, quantity: 1 }
              ],
              total: 906
            },
            {
              id: "20230515002",
              status: "已完成",
              items: [
                { name: "英德红茶", price: 198, quantity: 2 }
              ],
              total: 406
            }
          ],
          message: '获取成功'
        });
      }, 500);
    });
  },

  // 获取优惠券列表
  getCoupons: () => {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          code: 200,
          data: [
            {
              id: 1,
              discount: "八折",
              description: "全场通用",
              expiry: "2023-12-31"
            },
            {
              id: 2,
              discount: "满100减20",
              description: "茶叶类商品",
              expiry: "2023-10-31"
            }
          ],
          message: '获取成功'
        });
      }, 500);
    });
  },

  // 提交订单
  submitOrder: (order) => {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          code: 200,
          data: {
            orderId: `2023${Date.now()}`,
            status: "待支付"
          },
          message: '订单提交成功'
        });
      }, 500);
    });
  },

  // 支付订单
  payOrder: (orderId) => {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          code: 200,
          data: {
            orderId: orderId,
            status: "已支付"
          },
          message: '支付成功'
        });
      }, 500);
    });
  }
};

export default mockApi;