'use strict'
let fs = require('fs')
let Feed = require('feed')

let feed = new Feed({
  title: 'AEKOS API news',
  description: 'Updated related to the AEKOS API',
  id: 'http://api.aekos.org.au/',
  link: 'http://api.aekos.org.au/',
  generator: 'awesome', // optional, default = 'Feed for Node.js' 
  feedLinks: {
    atom: 'http://www.api.aekos.org.au/feed.atom',
  }
})

let posts = [
  {
    title: 'Version 1 released',
    url: 'https://github.com/adelaideecoinformatics/aekos-api/releases/tag/1.0.0-rc1',
    date: new Date(2016, 10, 7)
  }
]

posts.forEach(post => {
  feed.addItem({
    title: post.title,
    id: post.url,
    link: post.url,
    date: post.date
  })
})

const rssFileName = __dirname + '/feed.rss'
fs.writeFile(rssFileName, feed.rss2(), function(error) {
    if (error) {
        throw new Error('Failed to write RSS2 feed file', error)
    }
    console.log(`RSS2 feed written to '${rssFileName}'`)
})

const atomFileName = __dirname + '/feed.atom'
fs.writeFile(atomFileName, feed.atom1(), function(error) {
    if (error) {
        throw new Error('Failed to write Atom feed file', error)
    }
    console.log(`Atom feed written to '${atomFileName}'`)
})
