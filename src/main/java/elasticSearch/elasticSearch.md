一、基本介绍
    索引（index）相当于数据库，类型(type)相当于数据表，映射(Mapping)相当于数据表的表结构
    Mapping用来定义一个文档，可以定义所包含的字段以及字段的类型、分词器及属性等等
    
二、ES创建
    
三、数据结构
    1、查询：
    GET judge_doc/total_doc/_mapping
    2、插入:
四、查询统计
    es因为性能原因，有10000条限制，可以通过scroll绕过该限制
    例如：GET judge_doc/total_doc/_search?scroll=1m
五、查询统计
    1、无条件
    GET judge_doc/total_doc/_count
    2、有条件
    3、bool复合查询 -must：必须匹配，filter:匹配的结果过滤，should:至少有一个 must_not:不能匹配
    4、nested嵌套查询 boost-权重因子默认为1
    GET judge_doc/total_doc/_search
    {
      "size": 1000, 
      "query": {
        "nested": {
          "path": "litigants",
          "query": {
            "bool": {
              "must": [
                {"terms": {
                  "litigants.name.keyword": [
                    "小米科技有限责任公司"
                  ]
                }}
              ],"should": [
                {
                  "match": {
                    "litigants.status": "起诉方"
                  }
                }
              ],"should": [
                {
                  "match": {
                    "litigants.status": "应诉方"
                  }
                }
              ]
            }
          }
        }
      },
      "_source": {
        "includes": [
        "instrument_id","litigants.name","litigants.type","litigants.status"  
        ]
      }
    }
    GET judge_doc/total_doc/_search
    {
      "from" : 0,
      "size" : 1000,
      "timeout" : "30000ms",
      "query" : {
        "nested": {
          "path": "litigants",
          "query": {
            "bool" : {
              "filter" : [{
                "terms" : {
                  "litigants.name.keyword" : ["小米科技有限责任公司"],
                  "boost" : 1.0
                }
              }],
              "disable_coord" : false,
              "adjust_pure_negative" : true,
              "boost" : 1.0
            }
          }
        }
      },
      "_source" : {
        "includes" : [
            "court_name",
            "trial_date",
            "litigants",
            "case_no"
          ],
        "excludes" : [ ]
      },
      "sort" : [
        {
          "publish_date" : {
            "order" : "desc"
          }
        }
      ]
    }
    说明：排除法，例如：排除负面，遇到列表时，列表全部是负面才能被排除，而不是其中一个是负面
    被排除；如果想这样做必须再嵌套一个nested
    GET xinwen/_search
    {
      "size": 0, 
      "query": {
        "bool": {
          "must": [
            {
              "bool": {
                "must_not": [
                  {
                    "nested": {
                      "path": "organization2",
                      "query": {
                        "bool": {
                          "must": [
                            {
                              "match": {
                                "organization2.negative_class": "负面"
                              }
                            },
                            {
                              "match": {
                                "organization2.name": "中国建设银行股份有限公司"
                              }
                            }
                          ]
                        }
                      }
                    }
                  }
                ]
              }
            },
            {
              "bool": {
                "must": [
                  {
                    "nested": {
                      "path": "organization2",
                      "query": {
                        "bool": {
                          "must": [
                            {
                              "match": {
                                "organization2.negative_class": "争议"
                              }
                            },
                            {
                              "match": {
                                "organization2.name": "中国建设银行股份有限公司"
                              }
                            }
                          ]
                        }
                      }
                    }
                  }
                ]
              }
            }
          ],
          "filter": [
            {
              "range": {
                "lasttime": {
                  "from": "2018-06-01 16:21:00",
                  "to": "2018-12-01 17:34:59",
                  "include_lower": true,
                  "include_upper": true,
                  "format": "yyyy-MM-dd HH:mm:ss",
                  "boost": 1
                }
              }
            }
          ]
        }
      }
    }
    GET xinwen/_search
    {
      "size": 0, 
      "query": {
        "bool": {
          "must": [
            {
              "nested": {
                "path": "organization2",
                "query": {
                  "bool": {
                    "must": [
                      {
                        "terms": {
                          "organization2.name": [
                            "中国建设银行股份有限公司"
                          ]
                        }
                      },
                      {
                        "terms": {
                          "organization2.negative_class": [
                            "负面"
                          ]
                        }
                      }
                    ]
                  }
                }
              }
            }
          ],
          "filter": [
            {
              "range": {
                "lasttime": {
                  "from": "2018-06-01 16:21:00",
                  "to": "2018-12-01 17:34:59",
                  "include_lower": true,
                  "include_upper": true,
                  "format": "yyyy-MM-dd HH:mm:ss",
                  "boost": 1
                }
              }
            }
          ]
        }
      }
    }
    GET xinwen/_search
    {
      "size": 0,
      "query": {
        "bool": {
          "must": [
            {
               "nested": {
                 "path": "organization2",
                 "query": {
                   "bool": {
                     "must": [
                       {
                         "terms": {
                           "organization2.name": [
                             "中国建设银行股份有限公司"
                           ]
                         }
                       }
                     ]
                   }
                 }
               }
            },
            {
               "nested": {
                 "path": "organization2",
                 "query": {
                   "bool": {
                     "must": [
                       {
                         "terms": {
                           "organization2.eventList": [
                             "股权变化"
                           ]
                         }
                       }
                     ]
                   }
                 }
               }
            }
          ],
          "filter": [
            {
              "range": {
                "lasttime": {
                  "from": "2018-06-01 16:21:00",
                  "to": "2018-12-01 17:34:59",
                  "include_lower": true,
                  "include_upper": true,
                  "format": "yyyy-MM-dd HH:mm:ss",
                  "boost": 1
                }
              }
            }
          ],
          "disable_coord": false,
          "adjust_pure_negative": true,
          "boost": 1
        }
      },
      "_source": {
        "excludes": []
      }
    } 
    GET xinwen/_search
    {
      "size": 0,
      "query": {
        "bool": {
          "must": [
            {
               "nested": {
                 "path": "organization2",
                 "query": {
                   "bool": {
                     "must": [
                       {
                         "terms": {
                           "organization2.name": [
                             "中国建设银行股份有限公司"
                           ]
                         }
                       }
                     ]
                   }
                 }
               }
            }
          ],
          "filter": [
            {
              "range": {
                "lasttime": {
                  "from": "2018-06-01 16:21:00",
                  "to": "2018-12-01 17:34:59",
                  "include_lower": true,
                  "include_upper": true,
                  "format": "yyyy-MM-dd HH:mm:ss",
                  "boost": 1
                }
              }
            }
          ],
          "disable_coord": false,
          "adjust_pure_negative": true,
          "boost": 1
        }
      },
      "_source": {
        "excludes": []
      },
      "aggs": {
        "2": {
          "nested": {
            "path": "organization2"
          },
          "aggs": {
            "3": {
              "terms": {
                "field": "organization2.event_list",
                "size": 500,
                "order": {
                  "_count": "desc"
                }
              }
            }
          }
        }
      }
    }
    GET judge_doc/total_doc/_search
    {
      "size": 0,
      "query": {
        "nested": {
          "path": "litigants",
          "query": {
            "terms": {
              "litigants.name.keyword": [
                "上海浦东发展银行股份有限公司信用卡中心"
              ]
            }
          }
        }
      },
      "aggs": {
        "claim_count": {
          "range": {
            "field": "claim",
            "ranges": [
              {
                "to": 100000,
                "key": "10万以下"
              },
              {
                "from": 100000,
                "to": 500000,
                "key": "10-50万"
              },
              {
                "from": 500000,
                "to": 1000000,
                "key": "50-100万"
              },
              {
                "from": 1000000,
                "to": 10000000,
                "key": "100-1000万"
              },
              {
                "from": 10000000,
                "to": 100000000,
                "key": "1000万以上"
              },
              {
                "from": 100000000,
                "key": "1亿以上"
              }
            ]
          }
        },
        "verdict_claim_count": {
          "range": {
            "field": "verdict_claim",
            "ranges": [
              {
                "to": 100000,
                "key": "10万以下"
              },
              {
                "from": 100000,
                "to": 500000,
                "key": "10-50万"
              },
              {
                "from": 500000,
                "to": 1000000,
                "key": "50-100万"
              },
              {
                "from": 1000000,
                "to": 10000000,
                "key": "100-1000万"
              },
              {
                "from": 10000000,
                "to": 100000000,
                "key": "1000万以上"
              },
              {
                "from": 100000000,
                "key": "1亿以上"
              }
            ]
          }
        },
        "court_level_count": {
          "terms": {
            "field": "court_level",
            "size": 10,
            "exclude": ""
          }
        },
        "reason_count": {
          "terms": {
            "field": "reasons.reason.keyword",
            "size": 10000
          },
          "aggs": {
            "doc_type_count": {
              "terms": {
                "field": "content_type",
                "size": 100
              }
            }
          }
        },
        "litigants": {
          "nested": {
            "path": "litigants"
          },
          "aggs": {
            "name_count": {
              "terms": {
                "field": "litigants.name.keyword",
                "size": 10,
                "include": [
                  "上海翡翠东方传播有限公司"
                ]
              },
              "aggs": {
                "status_count": {
                  "terms": {
                    "field": "litigants.status",
                    "size": 10
                  },
                  "aggs": {
                    "type": {
                      "reverse_nested": {},
                      "aggs": {
                        "type_count": {
                          "terms": {
                            "field": "type",
                            "size": 1000
                          }
                        }
                      }
                    }
                  }
                },
                "type": {
                  "reverse_nested": {},
                  "aggs": {
                    "type_count": {
                      "terms": {
                        "field": "type",
                        "size": 1000
                      }
                    }
                  }
                },
                "verdict_date_count": {
                  "terms": {
                    "field": "litigants.verdict",
                    "size": 2,
                    "include": [
                      "胜诉",
                      "败诉"
                    ]
                  },
                  "aggs": {
                    "trial_date": {
                      "reverse_nested": {},
                      "aggs": {
                        "trial_date_count": {
                          "date_range": {
                            "field": "trial_date",
                            "ranges": [
                              {
                                "to": "2016-01-01",
                  "key": "2015及以前"
                              },
                              {
                                "from": "2016-01-01",
                                "to": "2017-01-01",
                  "key": "2016"
                              },
                              {
                                "from": "2017-01-01",
                                "to": "2018-01-01",
                  "key": "2017"
                              },
                              {
                                "from": "2018-01-01",
                                "to": "2019-01-01",
                  "key": "2018"
                              },
                              {
                                "from": "2019-01-01",
                                "to": "now",
                  "key": "2019"
                              }
                            ]
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    5、聚合查询 聚合中的missing是缺省的意思
    GET judge_doc/total_doc/_search
    {
    "size": 0, 
    "query": {
        "bool": {
            "must": {
                "exists":{
                "field":"trial_date"
                } 
            },
            "must":{
                "nested": {
                    "path": "litigants",
                    "query": {
                        "bool": {
                            "must":{
                                "terms": {
                                    "litigants.name.keyword": ["小米科技有限责任公司"]
                                }
                            }
                        }
                    }
                }
            },
            "filter": {
                "range": {
                    "trial_date": {
                        "format": "yyyy-MM-dd",
                        "gte": "2015-01-01",
                        "lte": "2018-01-01"
                    }
                }
            }
        }
    },
    "aggs": {
        "group_by_traildate": {
            "date_histogram": {
                "field": "trial_date",
                "format": "yyyy-MM",
                "interval": "month"
            }
        }
    }
    }
    GET judge_doc/total_doc/_search
    {
      "size": 0, 
      "query": {
        "nested": {
          "path": "litigants",
          "query": {
            "bool": {
              "must": [
                {
                  "terms": {
                    "litigants.name.keyword": [
                      "小米科技有限责任公司"
                    ]
                  }
                }
              ]
            }
          }
        }
      },
      "aggs": {
        "group_by_type": {
          "terms": {
            "field": "type",
            "missing": "其他",
            "size": 100
          }
        }
      },
      "aggs": {
        "group_by_content_type": {
          "terms": {
            "field": "content_type",
            "missing": "其他文书",
            "size": 100
          }
        }
      }
    }
    6、存在性查询
        GET land_sell_information/land_sell_information/_search
        {
          "query": {
            "exists":{
              "field":"land_num"
            }
          }
        }
        GET land_sell_information/land_sell_information/_search
        {
          "query": {
            "bool": {
              "must": {
                  "exists":{
                    "field":"land_num"
                  }
              }
            }
          }
        }
        GET land_sell_information/land_sell_information/_search
        {
            "query": {
                "bool": {
                    "must_not": {
                        "exists": {
                            "field": "land_num"
                        }
                    }
                }
            }
        }
        
六、更新类
    POST my_index/fulltext/1
    {"content":"美国留给伊拉克的是个烂摊子吗"}