const fs=require('node:fs'),vm=require('node:vm'),assert=require('node:assert/strict'),path=require('node:path');
const {stripTypeScriptTypes}=require('node:module');
let source=fs.readFileSync(path.join(__dirname,'index.ts'),'utf8').replace('export async function handler','async function handler');
const box={console,Date,Map,TextEncoder,Uint8Array,Response,Request,URL,Blob,FormData,AbortSignal,crypto:require('node:crypto').webcrypto,Deno:{serve(){}}};vm.createContext(box);vm.runInContext(stripTypeScriptTypes(source),box);const run=x=>vm.runInContext(x,box);
(async()=>{
 assert.equal(run('authorized({message:{chat:{type:"private",id:1015720586},from:{id:1015720586}}})'),true);
 assert.equal(run('authorized({message:{chat:{type:"group",id:1015720586},from:{id:1015720586}}})'),false);
 assert.equal(run('authorized({message:{chat:{type:"private",id:1015720586},from:{id:123}}})'),false);
 assert.match(run('quick("/ohm 230 46")'),/5.0000 A/);
 assert.match(run('quick("/threephase 415 10 1.2")'),/Check values/);
 assert.equal((await run('handler(new Request("https://example.com",{method:"POST",body:"{}"}))')).status,403);
 assert.equal((await run('handler(new Request("https://example.com"))')).status,200);
 box.calls=[];box.fetch=async(url,options)=>{box.calls.push({url,body:options?.body});if(url.includes('api.github'))return {ok:true,json:async()=>({tag_name:'v4',body:'<test>',html_url:'https://github.com/nandurpm/electro/releases/tag/v4'})};return {json:async()=>({ok:true,result:true})}};
 await run('handle("/changelog","test")');assert.match(JSON.parse(box.calls.at(-1).body).text,/&lt;test&gt;/);
 await run('handle("📲 Download APK","test")');assert.match(JSON.parse(box.calls.at(-1).body).text,/No matching download/);
 console.log('Webhook: 9 authorization, calculation, escaping and routing checks passed.');
})().catch(e=>{console.error(e);process.exitCode=1});
