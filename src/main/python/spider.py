# coding: utf-8

import requests
from bs4 import BeautifulSoup as soup


def extract_links(url):
    '''
    抽取南方周末的报道列表
    '''
    pass


def extract_article(url):
    '''
    南方周末的抽取：例如：http://www.infzm.com/content/117747
    '''
    response = requests.get(url)
    doc = soup(response.text, 'html5lib')
    title = doc.title.text[7:]
    content = doc.find_all("section", id="articleContent")[0].text.strip()
    tags = [tag.text for tag in doc.find_all("li", class_="tagContent")]
    return title, content, tags


if __name__ == '__main__':
    url = 'http://www.infzm.com/content/117747'
    title, content, tags = extract_article(url)
    print(title)
    print('----------------')
    print(tags)
    print('----------------')
    print(content)
